package at.ac.uibk.dps.pulse.local.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class FileUtil {

  public static void dumpStringToFile(String content, java.io.File file) throws IOException {
    final var filePath = file.toPath();

    final var parent = filePath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }

    Files.write(
      filePath,
      content.getBytes(),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    );
  }
}
