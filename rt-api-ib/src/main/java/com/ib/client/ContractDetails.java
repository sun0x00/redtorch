/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.util.List;

public class ContractDetails {
    private Contract m_contract;
    private String   m_marketName;
    private double   m_minTick;
    private int      m_priceMagnifier;
    private String   m_orderTypes;
    private String   m_validExchanges;
    private int      m_underConid;
    private String   m_longName;
    private String   m_contractMonth;
    private String   m_industry;
    private String   m_category;
    private String   m_subcategory;
    private String   m_timeZoneId;
    private String   m_tradingHours;
    private String   m_liquidHours;
    private String   m_evRule;
    private double   m_evMultiplier;
    private int      m_mdSizeMultiplier;
    private List<TagValue> m_secIdList; // CUSIP/ISIN/etc.
    private int      m_aggGroup;
    private String   m_underSymbol;
    private String   m_underSecType;
    private String   m_marketRuleIds;
    private String   m_realExpirationDate;
    private String   m_lastTradeTime;

    // BOND values
    private String   m_cusip;
    private String   m_ratings;
    private String   m_descAppend;
    private String   m_bondType;
    private String   m_couponType;
    private boolean  m_callable = false;
    private boolean  m_putable = false;
    private double   m_coupon = 0;
    private boolean  m_convertible = false;
    private String   m_maturity;
    private String   m_issueDate;
    private String   m_nextOptionDate;
    private String   m_nextOptionType;
    private boolean  m_nextOptionPartial = false;
    private String   m_notes;

    // Get
    public int conid()                  { return m_contract.conid(); }
    public Contract contract()          { return m_contract; }
    public String marketName()          { return m_marketName; }
    public double minTick()             { return m_minTick; }
    public int priceMagnifier()         { return m_priceMagnifier; }
    public String orderTypes()          { return m_orderTypes; }
    public String validExchanges()      { return m_validExchanges; }
    public int underConid()             { return m_underConid; }
    public String longName()            { return m_longName; }
    public String contractMonth()       { return m_contractMonth; }
    public String industry()            { return m_industry; }
    public String category()            { return m_category; }
    public String subcategory()         { return m_subcategory; }
    public String timeZoneId()          { return m_timeZoneId; }
    public String tradingHours()        { return m_tradingHours; }
    public String liquidHours()         { return m_liquidHours; }
    public String evRule()              { return m_evRule; }
    public double evMultiplier()        { return m_evMultiplier; }
    public int mdSizeMultiplier()       { return m_mdSizeMultiplier; }
    public List<TagValue> secIdList()   { return m_secIdList; }
    public int aggGroup()               { return m_aggGroup; }
    public String underSymbol()         { return m_underSymbol; }
    public String underSecType()        { return m_underSecType; }
    public String marketRuleIds()       { return m_marketRuleIds; }
    public String realExpirationDate()  { return m_realExpirationDate; }
    public String lastTradeTime()       { return m_lastTradeTime; }
    
    public String cusip()               { return m_cusip; }
    public String ratings()             { return m_ratings; }
    public String descAppend()          { return m_descAppend; }
    public String bondType()            { return m_bondType; }
    public String couponType()          { return m_couponType; }
    public boolean callable()           { return m_callable; }
    public boolean putable()            { return m_putable; }
    public double coupon()              { return m_coupon; }
    public boolean convertible()        { return m_convertible; }
    public String maturity()            { return m_maturity; }
    public String issueDate()           { return m_issueDate; }
    public String nextOptionDate()      { return m_nextOptionDate; }
    public String nextOptionType()      { return m_nextOptionType; }
    public boolean nextOptionPartial()  { return m_nextOptionPartial; }
    public String notes()               { return m_notes; }

    // Set
    public void contract(Contract contract)         { m_contract = contract; }
    public void marketName(String marketName)       { m_marketName = marketName; }
    public void minTick(double minTick)             { m_minTick = minTick; }
    public void priceMagnifier(int priceMagnifier)  { m_priceMagnifier = priceMagnifier; }
    public void orderTypes(String orderTypes)       { m_orderTypes = orderTypes; }
    public void validExchanges(String validExchanges) { m_validExchanges = validExchanges; }
    public void underConid(int underConid)          { m_underConid = underConid; }
    public void longName(String longName)           { m_longName = longName; }
    public void contractMonth(String contractMonth) { m_contractMonth = contractMonth; }
    public void industry(String industry)           { m_industry = industry; }
    public void category(String category)           { m_category = category; }
    public void subcategory(String subcategory)     { m_subcategory = subcategory; }    
    public void timeZoneId(String timeZoneId)       { m_timeZoneId = timeZoneId; }
    public void tradingHours(String tradingHours)   { m_tradingHours = tradingHours; }
    public void liquidHours(String liquidHours)     { m_liquidHours = liquidHours; }
    public void evRule(String evRule)               { m_evRule = evRule; }
    public void evMultiplier(double evMultiplier)   { m_evMultiplier = evMultiplier; }
    public void mdSizeMultiplier(int mdSizeMultiplier) { m_mdSizeMultiplier = mdSizeMultiplier; }
    public void secIdList(List<TagValue> secIdList) { m_secIdList = secIdList; }
    public void aggGroup(int aggGroup)              { m_aggGroup = aggGroup; }
    public void underSymbol(String underSymbol)     { m_underSymbol = underSymbol; }
    public void underSecType(String underSecType)   { m_underSecType = underSecType; }
    public void marketRuleIds(String marketRuleIds) { m_marketRuleIds = marketRuleIds; }
    public void realExpirationDate(String realExpirationDate) { m_realExpirationDate = realExpirationDate; }
    public void  lastTradeTime(String lastTradeTime) { m_lastTradeTime = lastTradeTime; }
    
    public void cusip(String cusip)             { m_cusip = cusip; }
    public void ratings(String ratings)         { m_ratings = ratings; }
    public void descAppend(String descAppend)   { m_descAppend = descAppend; }
    public void bondType(String bondType)       { m_bondType = bondType; }
    public void couponType(String couponType)   { m_couponType = couponType; }
    public void callable(boolean callable)      { m_callable = callable; }
    public void putable(boolean putable)        { m_putable = putable; }
    public void coupon(double coupon)           { m_coupon = coupon; }
    public void convertible(boolean convertible) { m_convertible = convertible; }
    public void maturity(String maturity)       { m_maturity = maturity; }
    public void issueDate(String issueDate)     { m_issueDate = issueDate; }
    public void nextOptionDate(String nextOptionDate) { m_nextOptionDate = nextOptionDate; }    
    public void nextOptionType(String nextOptionType) { m_nextOptionType = nextOptionType; }
    public void nextOptionPartial(boolean nextOptionPartial) { m_nextOptionPartial = nextOptionPartial; }
    public void notes(String notes)             { m_notes = notes; }

    public ContractDetails() {
        m_contract = new Contract();
        m_minTick = 0;
        m_underConid = 0;
        m_evMultiplier = 0;
    }

    public ContractDetails( Contract p_contract, String p_marketName, 
    		double p_minTick, String p_orderTypes, String p_validExchanges, int p_underConId, String p_longName,
    	    String p_contractMonth, String p_industry, String p_category, String p_subcategory,
    	    String p_timeZoneId, String	p_tradingHours, String p_liquidHours,
    	    String p_evRule, double p_evMultiplier, int p_mdSizeMultiplier, int p_aggGroup,
    	    String p_underSymbol, String p_underSecType, String p_marketRuleIds, String p_realExpirationDate, String p_lastTradeTime) {
        m_contract = p_contract;
    	m_marketName = p_marketName;
    	m_minTick = p_minTick;
    	m_orderTypes = p_orderTypes;
    	m_validExchanges = p_validExchanges;
    	m_underConid = p_underConId;
    	m_longName = p_longName;
        m_contractMonth = p_contractMonth;
        m_industry = p_industry;
        m_category = p_category;
        m_subcategory = p_subcategory;
        m_timeZoneId = p_timeZoneId;
        m_tradingHours = p_tradingHours;
        m_liquidHours = p_liquidHours;
        m_evRule = p_evRule;
        m_evMultiplier = p_evMultiplier;
        m_mdSizeMultiplier = p_mdSizeMultiplier;
        m_aggGroup = p_aggGroup;
        m_underSymbol = p_underSymbol;
        m_underSecType = p_underSecType;
        m_marketRuleIds = p_marketRuleIds;
        m_realExpirationDate = p_realExpirationDate;
        m_lastTradeTime = p_lastTradeTime;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder( m_contract.toString() );

        add( sb, "marketName", m_marketName);
        add( sb, "minTick", m_minTick);
        add( sb, "priceMagnifier", m_priceMagnifier);
        add( sb, "orderTypes", m_orderTypes);
        add( sb, "validExchanges", m_validExchanges);
        add( sb, "underConId", m_underConid);
        add( sb, "longName", m_longName);
        add( sb, "contractMonth", m_contractMonth);
        add( sb, "industry", m_industry);
        add( sb, "category", m_category);
        add( sb, "subcategory", m_subcategory);
        add( sb, "timeZoneId", m_timeZoneId);
        add( sb, "tradingHours", m_tradingHours);
        add( sb, "liquidHours", m_liquidHours);
        add( sb, "evRule", m_evRule);
        add( sb, "evMultiplier", m_evMultiplier);
        add( sb, "mdSizeMultiplier", m_mdSizeMultiplier);
        add( sb, "aggGroup", m_aggGroup);
        add( sb, "underSymbol", m_underSymbol);
        add( sb, "underSecType", m_underSecType);
        add( sb, "marketRuleIds", m_marketRuleIds);
        add( sb, "realExpirationDate", m_realExpirationDate);
        add( sb, "lastTradeTime", m_lastTradeTime);

        add( sb, "cusip", m_cusip);
        add( sb, "ratings", m_ratings);
        add( sb, "descAppend", m_descAppend);
        add( sb, "bondType", m_bondType);
        add( sb, "couponType", m_couponType);
        add( sb, "callable", m_callable);
        add( sb, "putable", m_putable);
        add( sb, "coupon", m_coupon);
        add( sb, "convertible", m_convertible);
        add( sb, "maturity", m_maturity);
        add( sb, "issueDate", m_issueDate);
        add( sb, "nextOptionDate", m_nextOptionDate);
        add( sb, "nextOptionType", m_nextOptionType);
        add( sb, "nextOptionPartial", m_nextOptionPartial);
        add( sb, "notes", m_notes);

        return sb.toString();
    }

    public static void add(StringBuilder sb, String tag, Object val) {
        if (val == null || val instanceof String && ((String)val).length() == 0) {
            return;
        }
        sb.append( tag);
        sb.append( '\t');
        sb.append( val);
        sb.append( '\n');
    }
}
