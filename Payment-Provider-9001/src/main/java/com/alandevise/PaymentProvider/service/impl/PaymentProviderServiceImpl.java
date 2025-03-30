package com.alandevise.PaymentProvider.service.impl;

import com.alandevise.api.pay.PaymentReqTest;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @Filename: PaymentProviderServiceImpl.java
 * @Package: com.alandevise.PaymentProvider.service.impl
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2025年03月30日 15:51
 */

@DubboService
public class PaymentProviderServiceImpl implements PaymentReqTest {

    @Override
    public String func() {
        return "This is a payment provider";
    }
}
