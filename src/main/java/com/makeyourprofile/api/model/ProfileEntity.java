package com.makeyourprofile.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileEntity {

    @Id
    private String id;

    @Indexed
    @Field("user_id")
    private String userId;

    @Builder.Default
    private String title = "Untitled Profile";

    @Field("profile_data")
    private String profileData;

    @Builder.Default
    @Field("theme_config")
    private String themeConfig = "{\"theme\": \"light\", \"accentColor\": \"#3b82f6\"}";

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;
}
