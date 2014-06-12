package org.opencb.cellbase.core.common.drug;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/9/13
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class DrugPartnerInteraction {
    private Drug drug;
    private Partner partner;
    private String type;
    private String knownAction;
    private List<String> action;

    public DrugPartnerInteraction() {
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public String getKnownAction() {
        return knownAction;
    }

    public void setKnownAction(String knownAction) {
        this.knownAction = knownAction;
    }
}
