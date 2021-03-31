package org.example;

import javafx.concurrent.Task;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixTask extends Task<Void> {
    final private long FULL_PROCENT = 100;
    final private String path;
    private StringBuilder newWord = new StringBuilder();
    final private String[] stringSearch = {"Bold", "Italic"};
    final private Map<String, List<Integer>> dictionary = new HashMap<>();

    public FixTask(String path) {
        this.path = path;
    }

    @Override
    protected Void call() throws Exception {
        File file = new File(path);
        if (file.isDirectory()) {
            FixFiles(file);
        }
        return null;
    }

    @Override
    protected void cancelled() {
        updateScene();
    }

    @Override
    protected void succeeded() {
        updateScene();
    }

    private void updateScene() {
        updateProgress(FULL_PROCENT, FULL_PROCENT);
    }

    private void FixFiles(File directory) {
        File[] files = directory.listFiles((name) -> name.getName().endsWith(".pdf"));
        long countFiles = 0;
        long currentProgress;
        if (isCancelled()) {
            return;
        }

        if (files != null) {
            for (File file :
                    files) {
                String newFileName = file.getAbsolutePath().replaceFirst("[.][^.]+$", "") + "-saved.pdf";

                try (PDDocument doc = PDDocument.load(file)) {
                    PDPageTree pages = doc.getPages();
                    for (PDPage page : pages) {
                        for (COSName name : page.getResources().getFontNames()) {
                            PDFont font = page.getResources().getFont(name);
                            String fontName = font.getName();
                            String newFontName = NameWithoutDuplicate(fontName);
                            if (font instanceof PDType0Font && !fontName.equals(newFontName)) {
                                PDType0Font type0font = (PDType0Font) font;
                                type0font.getCOSObject().setString(COSName.BASE_FONT, newFontName);
                                PDCIDFont descendantFont = type0font.getDescendantFont();
                                descendantFont.getCOSObject().setString(COSName.BASE_FONT, newFontName);
                                PDFontDescriptor fontDescriptor = descendantFont.getFontDescriptor();
                                fontDescriptor.setFontName(newFontName);
                            }
                        }
                    }
                    doc.save(new File(newFileName));
                } catch (IOException e) {

                }
                currentProgress = ++countFiles * FULL_PROCENT / files.length;
                updateProgress(currentProgress, FULL_PROCENT);
            }
        }

    }

    private String NameWithoutDuplicate(String source) {
        newWord = new StringBuilder(source);
        dictionary.clear();

        for (String str :
                stringSearch) {
            int pos = 0;
            List<Integer> indexFoundedStrings = new ArrayList<>();
            while (pos != -1) {
                pos = source.indexOf(str, pos);
                if (pos != -1) {
                    indexFoundedStrings.add(pos);
                    pos += 1;
                }
            }
            if (indexFoundedStrings.size() > 0) {
                dictionary.put(str, indexFoundedStrings);
            }
        }
        if (dictionary.size() > 0) {
            for (Map.Entry<String, List<Integer>> entry :
                    dictionary.entrySet()) {
                if (entry.getValue().size() > 1) {
                    newWord = new StringBuilder(newWord.toString().replace(entry.getKey(), ""));
                    newWord.insert(entry.getValue().get(0), entry.getKey());
                }
            }
            if (newWord.length() > 0)
                return newWord.toString();
        }
        return source;
    }
}
