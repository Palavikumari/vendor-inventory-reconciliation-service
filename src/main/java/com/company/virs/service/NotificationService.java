package com.company.virs.service;

import com.company.virs.entity.VendorInventory;

public interface NotificationService {

    void sendNotification(
            VendorInventory vendorInventory);
}