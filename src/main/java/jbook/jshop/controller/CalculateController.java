package jbook.jshop.controller;

import jbook.jshop.dto.ApiResponse;
import jbook.jshop.dto.CalculateInDto;
import jbook.jshop.dto.CalculateOutDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CalculateController {

    @PostMapping("/calculatePremium")
    public ResponseEntity<ApiResponse> calculatePremium(@RequestBody CalculateInDto in) {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setData(mockCalculateResult(in));
        response.setMessage("");
        return ResponseEntity.ok(response);
    }

    private CalculateOutDto mockCalculateResult(CalculateInDto in) {
        /*
        {
            "inputObj" : {"productType" : "4"},
            "resultArray" :
                [
                    {
                        "premium" : "100000",
                        "insTerm" : "2",
                        "napTerm" : "3",
                        "treatyList" : [
                            { name : "주보험", amt : 100000},
                            { name : "특약1", amt : 200000},
                            { name : "특약2", amt : 300000},
                        ]
                    }
                ]
        }
        */


        JSONArray treatyList = new JSONArray();
        List<String> nameList = Arrays.asList("주보험", "특약1", "특약2");
        List<String> amtList = Arrays.asList("100000", "200000", "200000");

        for (int i = 0; i < nameList.size(); i++) {
            JSONObject treaty = new JSONObject();
            treaty.put("name", nameList.get(i));
            treaty.put("amt", amtList.get(i) + " / 가입금액 : " + in.getPremium());
            treatyList.put(treaty);
        }

        JSONObject data = new JSONObject();
        data.put("premium", in.getPremium());
        data.put("insTerm", "2");
        data.put("napTerm", "3");
        data.put("treatyList", treatyList);

        JSONArray resultList = new JSONArray();
        resultList.put(data);
        ArrayList<Object> resultArray = new ArrayList<>(resultList.toList());


        CalculateOutDto out = CalculateOutDto.builder()
                .inputObj(in)
                .resultArray(resultArray)
                .build();
        return out;
    }
}
