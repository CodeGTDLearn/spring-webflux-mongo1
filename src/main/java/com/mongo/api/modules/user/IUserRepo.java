package com.mongo.api.modules.user;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository("userRepo")
public interface IUserRepo extends ReactiveMongoRepository<User, String> {



}
