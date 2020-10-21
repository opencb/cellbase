
/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Usage:
//    mongo -u ADMIN -p ADMIN_PASSWORD --authenticationDatabase admin --host HOST admin build/mongodb-scripts/grant-users.js

var ownerUser = "cellbase";
var readUser = "biouser";

var databases = [
  "cellbase_agambiae_agamp4_v3",
  "cellbase_cfamiliaris_canfam3_1_v3",
  "cellbase_dmelanogaster_bdgp6_v3",
  "cellbase_hsapiens_grch38_v3",
  "cellbase_olatipes_hdrr_v3",
  "cellbase_scerevisiae_r64_1_1_v3",
  "cellbase_athaliana_tair10_v3",
  "cellbase_csabaeus_chlsab1_1_v3",
  "cellbase_ggallus_galgal4_v3",
  "cellbase_lmajor_asm272v2_v3",
  "cellbase_oaries_oar_v3_1_v3",
  "cellbase_slycopersicum_sl2_40_v3",
  "cellbase_afumigatus_cadre_v3",
  "cellbase_cintestinalis_kh_v3",
  "cellbase_gmax_v1_0_v3",
  "cellbase_mmulatta_mmul_1_0_v3",
  "cellbase_ptroglodytes_chimp2_1_4_v3",
  "cellbase_sscrofa_sscrofa10_2_v3",
  "cellbase_btaurus_umd3_1_v3",
  "cellbase_ggorilla_gorgor3_1_v3",
  "cellbase_mmusculus_grcm38_v3",
  "cellbase_pfalciparum_asm276v1_v3",
  "cellbase_vvinifera_iggp_12x_v3",
  "cellbase_celegans_wbcel235_v3",
  "cellbase_drerio_zv9_v3",
  "cellbase_hsapiens_grch37_v3",
  "cellbase_osativa_irgsp-1_0_v3",
  "cellbase_rnorvegicus_rnor_5_0_v3",
  "cellbase_zmays_agpv3_v3"
];


for (var d in databases) {
  db.grantRolesToUser(readUser, [{role: "read", db: databases[d]}]);
  db.grantRolesToUser(ownerUser, [{role: "dbOwner", db: databases[d]}]);
}

