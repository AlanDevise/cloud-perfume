package com.alandevise.multitsdb.service;

import com.alandevise.tsdb.core.MultiTSDBTemplate;
import org.springframework.stereotype.Service;

@Service
public class TDEngineService extends BaseTSDBService {

    public TDEngineService(MultiTSDBTemplate multiTSDBTemplate) {
        super(multiTSDBTemplate);
    }

    @Override
    protected String getTsdbType() {
        return "tdengine";
    }
}
