package com.lemonoflime.rpbackground.services;

import com.lemonoflime.rpbackground.persistence.SimpleObject;
import ratpack.exec.Operation;
import ratpack.exec.Promise;
import ratpack.service.Service;

public interface StorageService extends Service {
    Promise<SimpleObject> retrieve(String id);
    Operation persist(SimpleObject simpleObject);
    Operation delete(String id);
}
