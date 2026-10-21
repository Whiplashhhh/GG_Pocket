package fr.gravelinesgrimp.pocket.topo.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

/** Route photos: uploaded by setters, cleaned without cropping, shown to every member. */
@IntegrationTest
class RoutePhotoIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;

    @Test
    void aSetterPhotographsARouteThatEveryMemberSees() throws Exception {
        var setter = members.create(Role.SETTER);
        var route = routes.create(setter);
        var detail =
                upload(setter, route, png(2400, 1200))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.route.photoUrl").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String photoUrl = JsonPath.read(detail, "$.route.photoUrl");

        var image =
                mockMvc.perform(get(photoUrl).with(as(members.create(Role.CLIMBER))))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray();
        var decoded = ImageIO.read(new ByteArrayInputStream(image));
        assertThat(decoded.getWidth()).isEqualTo(1600);
        assertThat(decoded.getHeight()).isEqualTo(800);
        mockMvc.perform(get("/api/v1/routes").with(as(members.create(Role.CLIMBER))))
                .andExpect(
                        jsonPath("$[?(@.id == '" + route.id() + "')].photoUrl")
                                .value(contains(photoUrl)));
    }

    @Test
    void aNewPhotoReplacesTheOldOne() throws Exception {
        var setter = members.create(Role.SETTER);
        var route = routes.create(setter);
        String first =
                JsonPath.read(
                        upload(setter, route, png(800, 600))
                                .andReturn()
                                .getResponse()
                                .getContentAsString(StandardCharsets.UTF_8),
                        "$.route.photoUrl");
        upload(setter, route, png(600, 800)).andExpect(status().isOk());

        mockMvc.perform(get(first).with(as(setter))).andExpect(status().isNotFound());

        mockMvc.perform(
                        delete("/api/v1/routes/" + route.id() + "/photo")
                                .with(as(setter))
                                .with(xsrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/routes/" + route.id()).with(as(setter)))
                .andExpect(jsonPath("$.route.photoUrl").doesNotExist());
    }

    @Test
    void onlySettersAndAboveUploadOnRoutesOnTheWall() throws Exception {
        var route = routes.create(members.create(Role.SETTER));
        upload(members.create(Role.CLIMBER), route, png(300, 300))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        delete("/api/v1/routes/" + route.id() + "/photo")
                                .with(as(members.create(Role.CLIMBER)))
                                .with(xsrf()))
                .andExpect(status().isForbidden());

        var dismounted = routes.create(members.create(Role.SETTER), RouteStatus.DISMOUNTED);
        upload(members.create(Role.BOARD), dismounted, png(300, 300))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_DISMOUNTED"));
    }

    @Test
    void anythingButAnImageIsRefused() throws Exception {
        var setter = members.create(Role.SETTER);
        var fake =
                new MockMultipartFile(
                        "file",
                        "voie.jpg",
                        "image/jpeg",
                        "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));
        upload(setter, routes.create(setter), fake)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IMAGE_UNSUPPORTED"));
    }

    private ResultActions upload(Member actor, Route route, MockMultipartFile file)
            throws Exception {
        var request =
                multipart(HttpMethod.PUT, "/api/v1/routes/" + route.id() + "/photo").file(file);
        request.with(as(actor)).with(xsrf());
        return mockMvc.perform(request);
    }

    private static MockMultipartFile png(int width, int height) throws Exception {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(new Color(0x7B, 0x4F, 0xB8));
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("file", "voie.png", "image/png", output.toByteArray());
    }
}
