package io.github.mlarsen_source.centroid_finder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;


public class CsvWriterTest {

    @TempDir
    Path tempDir;

    private static List<TimedCoordinate> sampleData() {
        return List.of(
            new TimedCoordinate(1.0, new Coordinate(2, 3)),
            new TimedCoordinate(2.0, new Coordinate(4, 6))
        );
    }

    @Test
    void writeToCsv_createsFileSuccessfully() throws IOException {
        CsvWriter writer = new CsvWriter();
        Path output = tempDir.resolve("output.csv");

        assertDoesNotThrow(() -> writer.writeToCsv(output.toString(), sampleData()));
        assertTrue(Files.exists(output), "Output CSV should exist");
        String content = Files.readString(output);
        assertTrue(content.contains("2,3"), "CSV should contain coordinate data");
    }

    @Test
    void writeToCsv_overwritesExistingFile() throws IOException {
        CsvWriter writer = new CsvWriter();
        Path output = tempDir.resolve("overwrite.csv");

        Files.writeString(output, "old data");
        assertTrue(Files.exists(output));

        writer.writeToCsv(output.toString(), sampleData());
        String content = Files.readString(output);
        assertFalse(content.contains("old data"), "File should be overwritten");
    }

    @Test
    void writeToCsv_handlesInvalidPath_gracefullyWithoutCrash() {
        CsvWriter writer = new CsvWriter();
        String invalidPath = "Z:/this/path/should/not/exist/output.csv";
        assertDoesNotThrow(() -> writer.writeToCsv(invalidPath, sampleData()),
            "CsvWriter should log error but not throw for invalid path");
    }

    @Test
    void writeToCsv_handlesNullOrEmptyPathGracefully() {
        CsvWriter writer = new CsvWriter();
        List<TimedCoordinate> data = sampleData();

        assertDoesNotThrow(() -> writer.writeToCsv(null, data),
            "CsvWriter should handle null path without throwing");
        assertDoesNotThrow(() -> writer.writeToCsv("", data),
            "CsvWriter should handle empty path without throwing");
    }

    @Test
    void writeToCsv_writesEmptyListGracefully() throws IOException {
        CsvWriter writer = new CsvWriter();
        Path output = tempDir.resolve("empty.csv");

        assertDoesNotThrow(() -> writer.writeToCsv(output.toString(), Collections.emptyList()));
        assertTrue(Files.exists(output), "File should still be created");
        String content = Files.readString(output);
        assertTrue(content.isBlank(), "File should be empty for empty input list");
    }

    @Test
    void writeToCsv_createsFileEvenIfWriteFailsMidway() throws IOException {
        CsvWriter writer = new CsvWriter();
        Path output = tempDir.resolve("partial.csv");

        // List that throws RuntimeException when accessed
        List<TimedCoordinate> badList = new AbstractList<>() {
            boolean first = true;
            @Override
            public TimedCoordinate get(int index) {
                throw new RuntimeException("boom");
            }
            @Override
            public int size() {
                return 1;
            }
        };

        assertDoesNotThrow(() -> writer.writeToCsv(output.toString(), badList),
            "CsvWriter should log runtime exception but not throw");
        assertTrue(Files.exists(output), "File should exist even if writing failed midway");
    }
}
