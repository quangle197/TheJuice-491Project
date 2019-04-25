package com.example.quangle.myapplication;

public class PendingItemClass {
    String itemID;
    String buyerID;
    String sellerID;

    public PendingItemClass(){}

    public PendingItemClass(String item, String buyer, String seller)
    {
        this.itemID=item;
        this.buyerID=buyer;
        this.sellerID=seller;
    }

    public String getItemID()
    {
        return this.itemID;
    }

    public String getbuyerID()
    {
        return this.buyerID;
    }

    public String getsellerID()
    {
        return this.sellerID;
    }

}
