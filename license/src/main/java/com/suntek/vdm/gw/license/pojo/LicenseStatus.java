package com.suntek.vdm.gw.license.pojo;

import lombok.Data;

@Data
public class LicenseStatus {
    private Boolean hasLicense = false;
    private Boolean hasLicenseForWeLink = false;

    public LicenseStatus() {
    }

    public LicenseStatus(Boolean hasLicense, Boolean hasLicenseForWeLink) {
        this.hasLicense = hasLicense;
        this.hasLicenseForWeLink = hasLicenseForWeLink;
    }
}
