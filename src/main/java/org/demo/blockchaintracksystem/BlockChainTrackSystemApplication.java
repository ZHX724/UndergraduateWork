package org.demo.blockchaintracksystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.demo.blockchaintracksystem.mapper")
public class BlockChainTrackSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockChainTrackSystemApplication.class, args);
	}

}
