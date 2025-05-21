package post.service.be_post_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import post.service.be_post_service.grpc.TestPostRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestDto {
    private String name;

    public TestDto(TestPostRequest request) {
        this.name = request.getName();
    }
}
