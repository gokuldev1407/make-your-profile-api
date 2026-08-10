package com.makeyourprofile.api.repository;

import com.makeyourprofile.api.model.ProfileEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends MongoRepository<ProfileEntity, String> {
    List<ProfileEntity> findByUserId(String userId);
}
