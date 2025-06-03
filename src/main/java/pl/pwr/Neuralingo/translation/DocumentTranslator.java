package pl.pwr.Neuralingo.translation;

import java.io.File;
import java.io.IOException;

public interface DocumentTranslator {

    String translateDocument(File inputFile, String targetLanguage) throws IOException;
}
