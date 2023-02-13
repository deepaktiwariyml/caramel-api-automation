package com.api.common;

public class CaramelApiEndPoints {



    public static final String CAR_POST_SIGNIN ="/users/login";
    public static final String CAR_POST_CREATE_OFFER_BUYER ="/offers";

    public static final String CAR_POST_CREATE_OFFER_SELLER ="/composite/create";
    public static final String CAR_GET_OFFERS="/offers";
    public static final String CAR_PATCH_UPDATE_OFFER="/offers/:offerId";
    public static final String CAR_POST_CREATE_DEAL="/composite/create-and-link";
    public static final String CAR_GET_ACTIVE_TXNS="/transactions?type=active";
    public static final String CAR_GET_PREV_TXNS="/transactions?type=previous";
    public static final String CAR_DEL_CANCEL_TXN="/transactions/:txnId";
    public static final String CAR_PATCH_UPDATE_TXN="/transactions/:txnId";

    public static final String CAR_DEL_CANCEL_DEAL="/deals/:dealId";
    public static final String CAR_PATCH_UPDATE_DEAL="/deals/:dealId";
}
