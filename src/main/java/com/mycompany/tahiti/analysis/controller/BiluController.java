package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.annalysis.model.Bilu;
import com.mycompany.tahiti.annalysis.model.Person;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/bilus")
@Api(description = "Bi controller")
public class BiluController {
    @ResponseBody
    @GetMapping("/{biluId}")
    public Bilu getBiluById(@PathVariable("biluId") String biluId) {
        Bilu bilu = new Bilu();
        bilu.setId("ssdsdsdfdffd");
        bilu.setName("王大锤的笔录");
        bilu.setContent("我叫王大锤，我一开始只想着\n" +
                "不用多久 我就会升职加薪 当上总经理 出任CEO 迎娶白富美 走向人生巅峰 想想还有点小激动呢\n" +
                "万万没想到, 她说这人好像条狗啊\n" +
            "他说得好有道理 我竟无言以对");
        List<String> tags = Arrays.asList(new String[]{"狗", "CEO"});
        bilu.setTags(tags);
        Person person = new Person();
        person.setName("王大锤");
        person.setIdentity("32212324324235331X");
        person.setId("http://mycompany.ai.com/person/王大锤");
        person.setBirthDay("1988年6月14日");
        person.setGender("男");
        person.setPhone("18888888881");
        person.setRole("嫌疑人");
        bilu.getPersons().add(person);
        return bilu;
    }
}
