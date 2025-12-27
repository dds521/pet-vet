package com.petvetai.domain.doctor.model;

import java.util.Objects;

/**
 * 地址值对象
 * 
 * 包含地址信息和地理位置坐标，用于距离计算
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public class Address {
    
    /**
     * 省份
     */
    private final String province;
    
    /**
     * 城市
     */
    private final String city;
    
    /**
     * 区县
     */
    private final String district;
    
    /**
     * 详细地址
     */
    private final String detail;
    
    /**
     * 完整地址（省市区+详细地址）
     */
    private final String fullAddress;
    
    /**
     * 经度
     */
    private final Double longitude;
    
    /**
     * 纬度
     */
    private final Double latitude;
    
    /**
     * 地址编码（行政区划编码）
     */
    private final String addressCode;
    
    /**
     * 私有构造函数
     * 
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detail 详细地址
     * @param longitude 经度
     * @param latitude 纬度
     * @param addressCode 地址编码
     * @author daidasheng
     * @date 2024-12-27
     */
    private Address(String province, String city, String district, String detail,
                   Double longitude, Double latitude, String addressCode) {
        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.longitude = longitude;
        this.latitude = latitude;
        this.addressCode = addressCode;
        
        // 构建完整地址
        StringBuilder sb = new StringBuilder();
        if (province != null && !province.isEmpty()) {
            sb.append(province);
        }
        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }
        if (district != null && !district.isEmpty()) {
            sb.append(district);
        }
        if (detail != null && !detail.isEmpty()) {
            sb.append(detail);
        }
        this.fullAddress = sb.toString();
    }
    
    /**
     * 创建地址值对象
     * 
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detail 详细地址
     * @param longitude 经度
     * @param latitude 纬度
     * @param addressCode 地址编码
     * @return 地址值对象
     * @author daidasheng
     * @date 2024-12-27
     */
    public static Address of(String province, String city, String district, String detail,
                            Double longitude, Double latitude, String addressCode) {
        return new Address(province, city, district, detail, longitude, latitude, addressCode);
    }
    
    /**
     * 创建地址值对象（不含经纬度）
     * 
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detail 详细地址
     * @param addressCode 地址编码
     * @return 地址值对象
     * @author daidasheng
     * @date 2024-12-27
     */
    public static Address of(String province, String city, String district, String detail, String addressCode) {
        return new Address(province, city, district, detail, null, null, addressCode);
    }
    
    /**
     * 计算到指定坐标的距离（单位：公里）
     * 使用 Haversine 公式计算两点间的大圆距离
     * 
     * @param targetLongitude 目标经度
     * @param targetLatitude 目标纬度
     * @return 距离（公里）
     * @author daidasheng
     * @date 2024-12-27
     */
    public Double calculateDistance(Double targetLongitude, Double targetLatitude) {
        if (longitude == null || latitude == null || 
            targetLongitude == null || targetLatitude == null) {
            return null;
        }
        
        // 地球半径（公里）
        final double EARTH_RADIUS = 6371.0;
        
        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(latitude);
        double lat2Rad = Math.toRadians(targetLatitude);
        double deltaLatRad = Math.toRadians(targetLatitude - latitude);
        double deltaLonRad = Math.toRadians(targetLongitude - longitude);
        
        // Haversine 公式
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * 判断是否有地理位置信息
     * 
     * @return 是否有地理位置信息
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean hasLocation() {
        return longitude != null && latitude != null;
    }
    
    // Getters
    public String getProvince() {
        return province;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public String getFullAddress() {
        return fullAddress;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public String getAddressCode() {
        return addressCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(province, address.province) &&
               Objects.equals(city, address.city) &&
               Objects.equals(district, address.district) &&
               Objects.equals(detail, address.detail) &&
               Objects.equals(addressCode, address.addressCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(province, city, district, detail, addressCode);
    }
    
    @Override
    public String toString() {
        return fullAddress;
    }
}

