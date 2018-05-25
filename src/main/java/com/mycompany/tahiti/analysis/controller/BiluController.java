package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.model.BiluBaseInfo;
import com.mycompany.tahiti.analysis.model.BiluRichInfo;
import com.mycompany.tahiti.analysis.model.Person;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/bilus")
@Api(description = "Bilu controller")
public class BiluController {
    @ResponseBody
    @GetMapping("/{biluId}")
    public BiluRichInfo getBiluById(@PathVariable("biluId") String biluId) {
        BiluRichInfo bilu = new BiluRichInfo();
        bilu.setId("ssdsdsdfdffd");
        bilu.setName("王大锤的笔录");
        bilu.setContent("我叫王大锤，我一开始只想着\n" +
                "不用多久 我就会升职加薪 当上总经理 出任CEO 迎娶白富美 走向人生巅峰 想想还有点小激动呢\n" +
                "万万没想到, 她说这人好像条狗啊\n" +
            "他说得好有道理 我竟无言以对");
        List<String> tags = Arrays.asList(new String[]{"狗", "CEO"});
        bilu.setTags(tags);

        return bilu;
    }
}
