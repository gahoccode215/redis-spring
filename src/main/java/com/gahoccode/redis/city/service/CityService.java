package com.gahoccode.redis.city.service;

import com.gahoccode.redis.city.client.CityClient;
import com.gahoccode.redis.city.dto.City;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CityService {

    @Autowired
    private CityClient cityClient;

    private RMapReactive<String, City> cityMap;

    public CityService(RedissonReactiveClient client) {
        this.cityMap = client.getMap("city", new TypedJsonJacksonCodec(String.class, City.class));
    }

    public Mono<City> getCity(final String zipCode){
        return this.cityMap.get(zipCode)
                .onErrorResume(ex -> this.cityClient.getCity(zipCode));
    }

    //@Scheduled(fixedRate = 10_000)
    public void updateCity(){
        this.cityClient.getAll()
                .collectList()
                .map(list -> list.stream().collect(Collectors.toMap(City::getZip, Function.identity())))
                .flatMap(this.cityMap::putAll)
                .subscribe();
    }

}
