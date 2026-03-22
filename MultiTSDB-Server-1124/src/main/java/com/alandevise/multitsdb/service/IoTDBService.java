package com.alandevise.multitsdb.service;

import com.alandevise.tsdb.core.MultiTSDBTemplate;
import org.springframework.stereotype.Service;

@Service
public class IoTDBService extends BaseTSDBService {

    public IoTDBService(MultiTSDBTemplate multiTSDBTemplate) {
        super(multiTSDBTemplate);
    }

    @Override
    protected String getTsdbType() {
        return "iotdb";
    }
}
