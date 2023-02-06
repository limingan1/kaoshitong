package com.suntek.vdm.gw.smc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;

@ComponentScans({
        @ComponentScan(basePackages = {"com.suntek.vdm.gw.smc"}),
        @ComponentScan(basePackages = {"com.suntek.smc.esdk",
                }),
        })
@Configuration
@EnableAutoConfiguration
public class SmcApplication {

}
