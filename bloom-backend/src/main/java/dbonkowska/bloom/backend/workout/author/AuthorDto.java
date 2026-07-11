package dbonkowska.bloom.backend.workout.author;

public record AuthorDto(Long id, String name) {

    public static AuthorDto from(Author author) {
        return new AuthorDto(author.getId(), author.getName());
    }
}