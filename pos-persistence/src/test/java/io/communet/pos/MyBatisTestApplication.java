package io.communet.pos;

import io.communet.pos.common.model.TestModel;
import io.communet.pos.persistence.mapper.TestModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * mybatis test application
 *
 * @author linux_china
 */
@SpringBootApplication
public class MyBatisTestApplication implements CommandLineRunner {

    @Autowired
    public TestModelMapper testModelMapper;

    public static void main(String[] args){
        SpringApplication.run(MyBatisTestApplication.class,args);
    }

    @Override
    public void run(String... strings) throws Exception {
        TestModel model = new TestModel();
        model.setAge(19);
        model.setName("Lee John");
        testModelMapper.insert(model);
    }

}
