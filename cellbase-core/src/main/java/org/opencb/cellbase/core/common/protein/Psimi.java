package org.opencb.cellbase.core.common.protein;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/4/13
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Psimi {
    private String psimi;
    private String name;

    public Psimi() {
    }

    public Psimi(String psimi, String name) {
        this.psimi = psimi;
        this.name = name;
    }

    public String getPsimi() {
        return psimi;
    }

    public void setPsimi(String psimi) {
        this.psimi = psimi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
