package com.familyfinance.service;

import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.TransferType;
import org.springframework.stereotype.Component;

@Component
public class TransferTypeResolver {

    public TransferType resolve(Member sender, Member recipient) {
        if (sender.isParent() && recipient.isChild()) {
            return TransferType.PARENT_TO_CHILD;
        }
        if (sender.isParent() && recipient.isParent()) {
            return TransferType.PARENT_TO_PARENT;
        }
        if (sender.isChild() && recipient.isChild()) {
            return TransferType.CHILD_TO_CHILD;
        }
        return TransferType.CHILD_TO_PARENT;
    }
}
