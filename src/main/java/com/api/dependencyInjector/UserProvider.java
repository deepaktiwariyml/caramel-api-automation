package com.api.dependencyInjector;

import com.api.common.CaramelUtil;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class UserProvider implements Provider<Object> {

    @Inject
    private CaramelUtil bffUtil;

    @Inject
    @Named("envUrl")
    private String  envUrl;

    @Inject
    @Named("username")
    private String  username;

    @Inject
    @Named("password")
    private String  password;

    @Inject
    @Named("plateform")
    private String  plateform;

    @Inject
    @Named("plateformVersion")
    private String  plateformVersion;


    @Override
    public Object get() {
       return null;

    }
}
