package dev.omega24;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Has a database and data folder been initialized yet?");
        System.out.print("[Y/n]: ");
        String verification = scanner.next();

        if (!verification.equalsIgnoreCase("Y")) {
            System.out.println("Please initialize a database and data folder before using this program.");
            return;
        }

        System.out.println();
        System.out.print("Please enter the path of the new database file (do not app the .mv.db portion): ");
        String database = scanner.next();

        System.out.println();
        System.out.print("Please enter the path of the new data folder: ");
        String dataFolder = scanner.next();

        System.out.println();
        System.out.print("Please enter the path of old data folder: ");
        String oldDataFolder = scanner.next();

        Connection connection = DriverManager.getConnection("jdbc:h2:file:" + database);
        PreparedStatement statement;

        ObjectMapper mapper = new ObjectMapper();
        Path dataJson = Path.of(oldDataFolder, "data.json");
        JsonDatabase oldData = mapper.readValue(dataJson.toFile(), JsonDatabase.class);

        for (JsonDatabase.Project project : oldData.projects()) {
            String projectId = UUID.randomUUID().toString();

            statement = connection.prepareStatement("INSERT INTO PROJECT VALUES (?, ?);");
            statement.setString(1, projectId);
            statement.setString(2, project.name());
            statement.execute();

            for (JsonDatabase.Project.Version version : project.versions()) {
                String versionId = UUID.randomUUID().toString();

                statement = connection.prepareStatement("INSERT INTO VERSION VALUES (?, ?, ?);");
                statement.setString(1, versionId);
                statement.setString(2, version.name());
                statement.setString(3, projectId);
                statement.execute();

                for (JsonDatabase.Project.Version.Build build : version.builds()) {
                    String buildId = UUID.randomUUID().toString();
                    String result = build.result().equals("SUCCESS") ? "SUCCESS" : "FAILURE";

                    statement = connection.prepareStatement("INSERT INTO BUILD VALUES (?, ?, ?, ?, ?, ?, ?);");
                    statement.setString(1, buildId);
                    statement.setString(2, build.build());
                    statement.setString(3, result);
                    statement.setLong(4, build.timestamp());
                    statement.setLong(5, build.duration());
                    statement.setString(6, build.md5());
                    statement.setString(7, versionId);
                    statement.execute();

                    if (build.commits() != null) {
                        for (JsonDatabase.Project.Version.Build.Commit commit : build.commits()) {
                            String commitId = UUID.randomUUID().toString();

                            statement = connection.prepareStatement("INSERT INTO COMMIT VALUES (?, ?, ?, ?, ?, ?, ?);");
                            statement.setString(1, commitId);
                            statement.setString(2, commit.author());
                            statement.setString(3, commit.email());
                            statement.setString(4, commit.comment());
                            statement.setString(5, commit.hash());
                            statement.setLong(6, commit.timestamp());
                            statement.setString(7, buildId);
                            statement.execute();
                        }
                    }

                    String fileId = UUID.randomUUID().toString();
                    statement = connection.prepareStatement("INSERT INTO FILE VALUES (?, ?, ?, ?);");
                    statement.setString(1, fileId);
                    statement.setString(2, "application/java-archive"); // old papyrus only had java files
                    statement.setString(3, build.extension());
                    statement.setString(4, buildId);
                    statement.execute();

                    Path oldFile = Path.of(oldDataFolder, String.format("%s-%s-%s", project.name(), version.name(), build.build()));
                    if (Files.exists(oldFile)) {
                        Path newFile = Path.of(dataFolder, fileId);
                        Files.copy(oldFile, newFile);
                    }
                }
            }
        }
    }
}
