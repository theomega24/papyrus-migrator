package dev.omega24;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonDatabase(List<Project> projects) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Project(String name, List<Version> versions) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Version(String name, List<Build> builds) {

            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Build(String build, String result, Long duration, Long timestamp, String md5, String extension, List<Commit> commits) {

                @JsonIgnoreProperties(ignoreUnknown = true)
                public record Commit(String author, String comment, String hash, String email, Long timestamp) {
                }
            }
        }
    }
}
