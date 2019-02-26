// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.model.test;

import com.yahoo.config.model.producer.AbstractConfigProducer;
import com.yahoo.vespa.model.AbstractService;
import com.yahoo.vespa.model.NetworkPortAllocator;

/**
 * This is a service for testing the plugin exchange mechanism in the
 * vespamodel. It provides some data that are made public in the API
 * of the plugin that owns it.
 *
 * @author  gjoranv
 */
public class ApiService extends AbstractService implements com.yahoo.test.StandardConfig.Producer {

    private int numSimpleServices = 0;

    /**
     * Creates a new ApiService instance
     *
     * @param parent   The parent ConfigProducer.
     * @param name     Service name
     */
    public ApiService(AbstractConfigProducer<?> parent, String name) {
        super(parent, name);
    }

    public void getConfig(com.yahoo.test.StandardConfig.Builder builder) {
        builder.astring("apiservice");
        
    }
    
    public void setNumSimpleServices(int nss) {
        numSimpleServices = nss;
    }

    @Override public void allocatePorts(int start, NetworkPortAllocator from) { }
    @Override public int getPortCount() { return 0; }
}
