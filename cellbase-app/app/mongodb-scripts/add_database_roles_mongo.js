
// Notes:
//  1. This script assumes that 'biouser' and 'cellbase' users exist, you can create them executin in MongoDB:
//      db.createUser({user: "siteUserAdmin", pwd: "XXX", roles: [ { role: "userAdminAnyDatabase", db: "admin" }]})
//  Now set auth to true in /etc/mongodb.conf and restart MongoDB
//      db.createUser( { user: "siteRootAdmin", pwd: "XXX",roles: [ { role: "root", db: "admin" }]})
//      db.createUser({user: "biouser", pwd: "XXX", roles: []})
//      db.createUser({user: "cellbase", pwd: "XXX", roles: []})
//  2. 'biouser' will have rad only access while 'cellbase' will be the owner
//  3. To launch this script: mongo -u ADMIN_USER -p XXX --authenticationDatabase admin admin add_database roles_mongo.js

// Some variables defined
var READ_USER = 'biouser';
var OWNER_USER = 'cellbase';
var SPECIES_V3 = ["afumigatus_cadre", "agambiae_agamp4", "athaliana_tair10", "btaurus_umd31", "celegans_wbcel235", "cfamiliaris_canfam31",
    "cintestinalis_kh", "csabaeus_chlsab11", "dmelanogaster_bdgp6", "drerio_zv9", "ggallus_galgal4", "ggorilla_gorgor31", "gmax_v10",
    "hsapiens_grch37", "hsapiens_grch38", "lmajor_asm272v2", "mmulatta_mmul10", "mmusculus_grcm38", "oaries_oarv31", "olatipes_hdrr",
    "osativa_irgsp10", "pfalciparum_asm276v1", "ptroglodytes_chimp214", "rnorvegicus_rnor50", "sbicolor_sorbi1", "scerevisiae_r6411",
    "slycopersicum_sl240", "sscrofa_sscrofa102", "vvinifera_iggp12x", "zmays_agpv3"];
var SPECIES_V4 = ["afumigatus_cadre", "agambiae_agamp4", "athaliana_tair10", "btaurus_umd31", "celegans_wbcel235", "cfamiliaris_canfam31",
    "cintestinalis_kh", "csabaeus_chlsab11", "dmelanogaster_bdgp6", "drerio_grcz10", "ggallus_galgal4", "ggorilla_gorgor31", "gmax_v10",
    "hsapiens_grch37", "hsapiens_grch38", "lmajor_asm272v2", "mmulatta_mmul10", "mmusculus_grcm38", "oaries_oarv31", "olatipes_hdrr",
    "osativa_irgsp10", "pfalciparum_asm276v1", "ptroglodytes_chimp214", "rnorvegicus_rnor60", "sbicolor_sorbi1", "scerevisiae_r6411",
    "slycopersicum_sl240", "sscrofa_sscrofa102", "vvinifera_iggp12x", "zmays_agpv3"];
var DATABASES = [];

// we add v3 and v4 databases
for (var s in SPECIES_V3) {
    DATABASES.push("cellbase_" + SPECIES_V3[s] + "_v3");
}
for (var s in SPECIES_V4) {
    DATABASES.push("cellbase_" + SPECIES_V4[s] + "_v4");
}

// Aux variables
var readUser;
var ownerUser;

// We get all users (from 'admin' database)
var users = db.getUsers();
for (var i in users) {
    if (users[i].user === READ_USER) {
        readUser = users[i];
    }
    if (users[i].user === OWNER_USER) {
        ownerUser = users[i];
    }
}

// if 'biouser' user is defined we added the role to the unasinged databases
if (readUser != undefined) {
    addDatabaseRolesToUser(readUser, "read");
}

// if 'cellbase' user is defined we added the role to the unasinged databases
if (ownerUser != undefined) {
    addDatabaseRolesToUser(ownerUser, "dbOwner");
}

function addDatabaseRolesToUser(user, role) {
    // if user is defined we added the role to the unasinged databases
    print ("User '" + user.user + "' found, adding roles to databases...");
    var count = 0;
    // We look for the existing databases in the current user
    for (var i in DATABASES) {
        var found = 0;
        for (var j in user.roles) {
            if (user.roles[j].db === DATABASES[i]) {
                found = 1;
            }
        }
        // Add only to not found databases (otherwise we will push to an array)
        if (found == 0) {
            print("\tAdding role '" + role + "' to database '" + DATABASES[i] + "'");
            db.grantRolesToUser(user.user, [{"role": role, "db" : DATABASES[i]}]);
            count++;
        }
    }
    print("\tNumber of databases added: '" + count + "'\n");
}