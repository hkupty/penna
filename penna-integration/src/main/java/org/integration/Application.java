package org.integration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;


public class Application {

  private record AuditLogMessage(
          String logger,
          String level,
          String message,
          String pennaVersion
  ) {}

  private static final Set<String> NO_ERRORS = Set.of("WARN", "ERROR");


  private static final PrintStream out;
  private static final PipedInputStream vIn = new PipedInputStream();
  private static final PipedOutputStream vOut;
  static {
    try {
      vOut = new PipedOutputStream(vIn);
      var testOut = new PrintStream(vOut);
      out = System.out;
      System.setOut(testOut);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }


  public static void main(String[] args) throws IOException {
    try {
      var logger = LoggerFactory.getLogger(Application.class);
      logger.info("Running something");
    } finally {
      System.setOut(out);
      vOut.close();
    }

    var om = new ObjectMapper();
    try {
      JsonParser parser = om.createParser(vIn);

      var results = om.readValues(parser, AuditLogMessage.class).readAll();
      assert results
      .stream()
      .noneMatch(auditLogMessage -> NO_ERRORS.contains(auditLogMessage.level))
      : "There should be no errors in initialization";
      assert results.size() == 1 : "There should be only one message";

      var result = results.getFirst();

      assert Objects.nonNull(result.pennaVersion) : "Version must be present";
      var base = Path.of(".").toAbsolutePath().getParent().getParent().resolve("version");
      try(var reader = Files.newBufferedReader(base)) {
        var baseVersion = reader.readLine();
        assert Objects.equals(result.pennaVersion, baseVersion) : String.format("%s does not match %s", result.pennaVersion, baseVersion);
      }
    } finally {
      vIn.close();
    }
  }
}
