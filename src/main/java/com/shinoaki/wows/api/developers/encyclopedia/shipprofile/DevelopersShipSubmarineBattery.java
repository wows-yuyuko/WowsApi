package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 潜艇电池：容量、消耗与回充速率。
 *
 * @author Xun
 */
public record DevelopersShipSubmarineBattery(double consumption_rate, double max_capacity, double regen_rate,
                                             int total) {
}
