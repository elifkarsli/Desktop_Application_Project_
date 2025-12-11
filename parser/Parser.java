package Desktop_Application_Project_.parser; // Added underscore

import Desktop_Application_Project_.exception.DataImportException; // Added underscore
import java.io.File;
import java.util.List;

public interface Parser<T> {
    List<T> parse(File file) throws DataImportException;
}