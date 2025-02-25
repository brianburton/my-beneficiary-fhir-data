-- The V40 through V52 migrations rename our tables and columns for CCW-sourced data, such that:
-- 1. We follow PostgreSQL's general snake_case naming conventions, to improve the developer experience: DB
--    object names won't have to be quoted all over the place, anymore.
-- 2. Column names match those in our upstream source system, the CCW, to improve traceability as data flows
--    through our systems.
-- 3. Rename the "parentXXX" foreign key columns to instead have names that match their target column.

-- Rename tables and table columns; syntax:
--
--      psql: alter table public.beneficiaries rename column "beneficiaryId" to bene_id;
--      hsql: alter table public.beneficiaries alter column  "beneficiaryId" rename to bene_id;
--
--      ${logic.alter-rename-column}
--          psql: "rename column"
--          hsql: "alter column"
--
--      ${logic.rename-to}
--          psql: "to"
--          hsql: "rename to"
--
-- OutpatientClaims to outpatient_claims
--
alter table public."OutpatientClaims" rename to outpatient_claims;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimId" ${logic.rename-to} clm_id;
alter table public.outpatient_claims ${logic.alter-rename-column} "beneficiaryId" ${logic.rename-to} bene_id;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimGroupId" ${logic.rename-to} clm_grp_id;
alter table public.outpatient_claims ${logic.alter-rename-column} lastupdated ${logic.rename-to} last_updated;
alter table public.outpatient_claims ${logic.alter-rename-column} "dateFrom" ${logic.rename-to} clm_from_dt;
alter table public.outpatient_claims ${logic.alter-rename-column} "dateThrough" ${logic.rename-to} clm_thru_dt;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimFacilityTypeCode" ${logic.rename-to} clm_fac_type_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimFrequencyCode" ${logic.rename-to} clm_freq_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "mcoPaidSw" ${logic.rename-to} clm_mco_pd_sw;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimNonPaymentReasonCode" ${logic.rename-to} clm_mdcr_non_pmt_rsn_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "paymentAmount" ${logic.rename-to} clm_pmt_amt;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimServiceClassificationTypeCode" ${logic.rename-to} clm_srvc_clsfctn_type_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "attendingPhysicianNpi" ${logic.rename-to} at_physn_npi;
alter table public.outpatient_claims ${logic.alter-rename-column} "attendingPhysicianUpin" ${logic.rename-to} at_physn_upin;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimQueryCode" ${logic.rename-to} claim_query_code;
alter table public.outpatient_claims ${logic.alter-rename-column} "fiscalIntermediaryClaimProcessDate" ${logic.rename-to} fi_clm_proc_dt;
alter table public.outpatient_claims ${logic.alter-rename-column} "fiDocumentClaimControlNumber" ${logic.rename-to} fi_doc_clm_cntl_num;
alter table public.outpatient_claims ${logic.alter-rename-column} "fiscalIntermediaryNumber" ${logic.rename-to} fi_num;
alter table public.outpatient_claims ${logic.alter-rename-column} "fiOriginalClaimControlNumber" ${logic.rename-to} fi_orig_clm_cntl_num;
alter table public.outpatient_claims ${logic.alter-rename-column} "finalAction" ${logic.rename-to} final_action;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternalFirstCode" ${logic.rename-to} fst_dgns_e_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternalFirstCodeVersion" ${logic.rename-to} fst_dgns_e_vrsn_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "beneficiaryPaymentAmount" ${logic.rename-to} clm_op_bene_pmt_amt;
alter table public.outpatient_claims ${logic.alter-rename-column} "coinsuranceAmount" ${logic.rename-to} nch_bene_ptb_coinsrnc_amt;
alter table public.outpatient_claims ${logic.alter-rename-column} "bloodDeductibleLiabilityAmount" ${logic.rename-to} nch_bene_blood_ddctbl_lblty_am;
alter table public.outpatient_claims ${logic.alter-rename-column} "deductibleAmount" ${logic.rename-to} nch_bene_ptb_ddctbl_amt;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimTypeCode" ${logic.rename-to} nch_clm_type_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "nearLineRecordIdCode" ${logic.rename-to} nch_near_line_rec_ident_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "claimPrimaryPayerCode" ${logic.rename-to} nch_prmry_pyr_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "primaryPayerPaidAmount" ${logic.rename-to} nch_prmry_pyr_clm_pd_amt;
alter table public.outpatient_claims ${logic.alter-rename-column} "professionalComponentCharge" ${logic.rename-to} nch_profnl_cmpnt_chrg_amt;
alter table public.outpatient_claims ${logic.alter-rename-column} "weeklyProcessDate" ${logic.rename-to} nch_wkly_proc_dt;
alter table public.outpatient_claims ${logic.alter-rename-column} "operatingPhysicianNpi" ${logic.rename-to} op_physn_npi;
alter table public.outpatient_claims ${logic.alter-rename-column} "operatingPhysicianUpin" ${logic.rename-to} op_physn_upin;
alter table public.outpatient_claims ${logic.alter-rename-column} "organizationNpi" ${logic.rename-to} org_npi_num;
alter table public.outpatient_claims ${logic.alter-rename-column} "otherPhysicianNpi" ${logic.rename-to} ot_physn_npi;
alter table public.outpatient_claims ${logic.alter-rename-column} "otherPhysicianUpin" ${logic.rename-to} ot_physn_upin;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisPrincipalCode" ${logic.rename-to} prncpal_dgns_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisPrincipalCodeVersion" ${logic.rename-to} prncpal_dgns_vrsn_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "providerNumber" ${logic.rename-to} prvdr_num;
alter table public.outpatient_claims ${logic.alter-rename-column} "providerStateCode" ${logic.rename-to} prvdr_state_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "patientDischargeStatusCode" ${logic.rename-to} ptnt_dschrg_stus_cd;
alter table public.outpatient_claims ${logic.alter-rename-column} "providerPaymentAmount" ${logic.rename-to} clm_op_prvdr_pmt_amt;
alter table public.outpatient_claims ${logic.alter-rename-column} "totalChargeAmount" ${logic.rename-to} clm_tot_chrg_amt;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisAdmission1Code" ${logic.rename-to} rsn_visit_cd1;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisAdmission1CodeVersion" ${logic.rename-to} rsn_visit_vrsn_cd1;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisAdmission2Code" ${logic.rename-to} rsn_visit_cd2;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisAdmission2CodeVersion" ${logic.rename-to} rsn_visit_vrsn_cd2;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisAdmission3Code" ${logic.rename-to} rsn_visit_cd3;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisAdmission3CodeVersion" ${logic.rename-to} rsn_visit_vrsn_cd3;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis1Code" ${logic.rename-to} icd_dgns_cd1;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis2Code" ${logic.rename-to} icd_dgns_cd2;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis3Code" ${logic.rename-to} icd_dgns_cd3;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis4Code" ${logic.rename-to} icd_dgns_cd4;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis5Code" ${logic.rename-to} icd_dgns_cd5;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis6Code" ${logic.rename-to} icd_dgns_cd6;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis7Code" ${logic.rename-to} icd_dgns_cd7;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis8Code" ${logic.rename-to} icd_dgns_cd8;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis9Code" ${logic.rename-to} icd_dgns_cd9;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis10Code" ${logic.rename-to} icd_dgns_cd10;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis11Code" ${logic.rename-to} icd_dgns_cd11;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis12Code" ${logic.rename-to} icd_dgns_cd12;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis13Code" ${logic.rename-to} icd_dgns_cd13;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis14Code" ${logic.rename-to} icd_dgns_cd14;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis15Code" ${logic.rename-to} icd_dgns_cd15;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis16Code" ${logic.rename-to} icd_dgns_cd16;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis17Code" ${logic.rename-to} icd_dgns_cd17;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis18Code" ${logic.rename-to} icd_dgns_cd18;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis19Code" ${logic.rename-to} icd_dgns_cd19;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis20Code" ${logic.rename-to} icd_dgns_cd20;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis21Code" ${logic.rename-to} icd_dgns_cd21;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis22Code" ${logic.rename-to} icd_dgns_cd22;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis23Code" ${logic.rename-to} icd_dgns_cd23;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis24Code" ${logic.rename-to} icd_dgns_cd24;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis25Code" ${logic.rename-to} icd_dgns_cd25;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal1Code" ${logic.rename-to} icd_dgns_e_cd1;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal2Code" ${logic.rename-to} icd_dgns_e_cd2;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal3Code" ${logic.rename-to} icd_dgns_e_cd3;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal4Code" ${logic.rename-to} icd_dgns_e_cd4;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal5Code" ${logic.rename-to} icd_dgns_e_cd5;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal6Code" ${logic.rename-to} icd_dgns_e_cd6;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal7Code" ${logic.rename-to} icd_dgns_e_cd7;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal8Code" ${logic.rename-to} icd_dgns_e_cd8;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal9Code" ${logic.rename-to} icd_dgns_e_cd9;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal10Code" ${logic.rename-to} icd_dgns_e_cd10;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal11Code" ${logic.rename-to} icd_dgns_e_cd11;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal12Code" ${logic.rename-to} icd_dgns_e_cd12;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal1CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd1;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal2CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd2;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal3CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd3;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal4CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd4;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal5CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd5;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal6CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd6;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal7CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd7;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal8CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd8;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal9CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd9;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal10CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd10;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal11CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd11;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosisExternal12CodeVersion" ${logic.rename-to} icd_dgns_e_vrsn_cd12;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis1CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd1;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis2CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd2;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis3CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd3;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis4CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd4;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis5CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd5;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis6CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd6;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis7CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd7;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis8CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd8;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis9CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd9;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis10CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd10;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis11CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd11;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis12CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd12;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis13CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd13;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis14CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd14;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis15CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd15;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis16CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd16;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis17CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd17;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis18CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd18;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis19CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd19;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis20CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd20;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis21CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd21;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis22CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd22;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis23CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd23;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis24CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd24;
alter table public.outpatient_claims ${logic.alter-rename-column} "diagnosis25CodeVersion" ${logic.rename-to} icd_dgns_vrsn_cd25;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure1Code" ${logic.rename-to} icd_prcdr_cd1;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure2Code" ${logic.rename-to} icd_prcdr_cd2;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure3Code" ${logic.rename-to} icd_prcdr_cd3;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure4Code" ${logic.rename-to} icd_prcdr_cd4;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure5Code" ${logic.rename-to} icd_prcdr_cd5;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure6Code" ${logic.rename-to} icd_prcdr_cd6;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure7Code" ${logic.rename-to} icd_prcdr_cd7;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure8Code" ${logic.rename-to} icd_prcdr_cd8;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure9Code" ${logic.rename-to} icd_prcdr_cd9;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure10Code" ${logic.rename-to} icd_prcdr_cd10;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure11Code" ${logic.rename-to} icd_prcdr_cd11;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure12Code" ${logic.rename-to} icd_prcdr_cd12;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure13Code" ${logic.rename-to} icd_prcdr_cd13;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure14Code" ${logic.rename-to} icd_prcdr_cd14;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure15Code" ${logic.rename-to} icd_prcdr_cd15;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure16Code" ${logic.rename-to} icd_prcdr_cd16;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure17Code" ${logic.rename-to} icd_prcdr_cd17;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure18Code" ${logic.rename-to} icd_prcdr_cd18;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure19Code" ${logic.rename-to} icd_prcdr_cd19;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure20Code" ${logic.rename-to} icd_prcdr_cd20;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure21Code" ${logic.rename-to} icd_prcdr_cd21;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure22Code" ${logic.rename-to} icd_prcdr_cd22;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure23Code" ${logic.rename-to} icd_prcdr_cd23;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure24Code" ${logic.rename-to} icd_prcdr_cd24;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure25Code" ${logic.rename-to} icd_prcdr_cd25;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure1CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd1;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure2CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd2;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure3CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd3;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure4CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd4;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure5CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd5;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure6CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd6;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure7CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd7;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure8CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd8;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure9CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd9;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure10CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd10;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure11CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd11;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure12CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd12;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure13CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd13;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure14CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd14;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure15CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd15;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure16CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd16;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure17CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd17;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure18CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd18;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure19CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd19;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure20CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd20;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure21CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd21;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure22CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd22;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure23CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd23;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure24CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd24;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure25CodeVersion" ${logic.rename-to} icd_prcdr_vrsn_cd25;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure1Date" ${logic.rename-to} prcdr_dt1;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure2Date" ${logic.rename-to} prcdr_dt2;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure3Date" ${logic.rename-to} prcdr_dt3;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure4Date" ${logic.rename-to} prcdr_dt4;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure5Date" ${logic.rename-to} prcdr_dt5;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure6Date" ${logic.rename-to} prcdr_dt6;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure7Date" ${logic.rename-to} prcdr_dt7;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure8Date" ${logic.rename-to} prcdr_dt8;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure9Date" ${logic.rename-to} prcdr_dt9;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure10Date" ${logic.rename-to} prcdr_dt10;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure11Date" ${logic.rename-to} prcdr_dt11;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure12Date" ${logic.rename-to} prcdr_dt12;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure13Date" ${logic.rename-to} prcdr_dt13;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure14Date" ${logic.rename-to} prcdr_dt14;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure15Date" ${logic.rename-to} prcdr_dt15;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure16Date" ${logic.rename-to} prcdr_dt16;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure17Date" ${logic.rename-to} prcdr_dt17;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure18Date" ${logic.rename-to} prcdr_dt18;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure19Date" ${logic.rename-to} prcdr_dt19;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure20Date" ${logic.rename-to} prcdr_dt20;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure21Date" ${logic.rename-to} prcdr_dt21;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure22Date" ${logic.rename-to} prcdr_dt22;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure23Date" ${logic.rename-to} prcdr_dt23;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure24Date" ${logic.rename-to} prcdr_dt24;
alter table public.outpatient_claims ${logic.alter-rename-column} "procedure25Date" ${logic.rename-to} prcdr_dt25;
--
-- OutpatientClaimLines to outpatient_claim_lines
--
alter table public."OutpatientClaimLines" rename to outpatient_claim_lines;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "parentClaim" ${logic.rename-to} clm_id;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "lineNumber" ${logic.rename-to} clm_line_num;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "nationalDrugCode" ${logic.rename-to} rev_cntr_ide_ndc_upc_num;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "hcpcsCode" ${logic.rename-to} hcpcs_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "hcpcsInitialModifierCode" ${logic.rename-to} hcpcs_1st_mdfr_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "hcpcsSecondModifierCode" ${logic.rename-to} hcpcs_2nd_mdfr_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "revenueCenterRenderingPhysicianNPI" ${logic.rename-to} rndrng_physn_npi;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "revenueCenterRenderingPhysicianUPIN" ${logic.rename-to} rndrng_physn_upin;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "revenueCenterCode" ${logic.rename-to} rev_cntr;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "revenueCenterDate" ${logic.rename-to} rev_cntr_dt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "paymentAmount" ${logic.rename-to} rev_cntr_pmt_amt_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "apcOrHippsCode" ${logic.rename-to} rev_cntr_apc_hipps_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "benficiaryPaymentAmount" ${logic.rename-to} rev_cntr_bene_pmt_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "bloodDeductibleAmount" ${logic.rename-to} rev_cntr_blood_ddctbl_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "cashDeductibleAmount" ${logic.rename-to} rev_cntr_cash_ddctbl_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "discountCode" ${logic.rename-to} rev_cntr_dscnt_ind_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "firstMspPaidAmount" ${logic.rename-to} rev_cntr_1st_msp_pd_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "nationalDrugCodeQualifierCode" ${logic.rename-to} rev_cntr_ndc_qty_qlfr_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "nationalDrugCodeQuantity" ${logic.rename-to} rev_cntr_ndc_qty;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "nonCoveredChargeAmount" ${logic.rename-to} rev_cntr_ncvrd_chrg_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "obligationToAcceptAsFullPaymentCode" ${logic.rename-to} rev_cntr_otaf_pmt_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "packagingCode" ${logic.rename-to} rev_cntr_packg_ind_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "patientResponsibilityAmount" ${logic.rename-to} rev_cntr_ptnt_rspnsblty_pmt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "paymentMethodCode" ${logic.rename-to} rev_cntr_pmt_mthd_ind_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "providerPaymentAmount" ${logic.rename-to} rev_cntr_prvdr_pmt_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "rateAmount" ${logic.rename-to} rev_cntr_rate_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "reducedCoinsuranceAmount" ${logic.rename-to} rev_cntr_rdcd_coinsrnc_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "revCntr1stAnsiCd" ${logic.rename-to} rev_cntr_1st_ansi_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "revCntr2ndAnsiCd" ${logic.rename-to} rev_cntr_2nd_ansi_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "revCntr3rdAnsiCd" ${logic.rename-to} rev_cntr_3rd_ansi_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "revCntr4thAnsiCd" ${logic.rename-to} rev_cntr_4th_ansi_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "secondMspPaidAmount" ${logic.rename-to} rev_cntr_2nd_msp_pd_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "statusCode" ${logic.rename-to} rev_cntr_stus_ind_cd;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "totalChargeAmount" ${logic.rename-to} rev_cntr_tot_chrg_amt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "unitCount" ${logic.rename-to} rev_cntr_unit_cnt;
alter table public.outpatient_claim_lines ${logic.alter-rename-column} "wageAdjustedCoinsuranceAmount" ${logic.rename-to} rev_cntr_coinsrnc_wge_adjstd_c;

-- psql only
${logic.psql-only-alter} index if exists public."OutpatientClaimLines_pkey" rename to outpatient_claim_lines_pkey;
${logic.psql-only-alter} index if exists public."OutpatientClaims_pkey" rename to outpatient_claims_pkey;

${logic.psql-only-alter} table public.outpatient_claim_lines rename constraint "OutpatientClaimLines_parentClaim_to_OutpatientClaims" to outpatient_claim_lines_clm_id_to_outpatient_claims;
${logic.psql-only-alter} table public.outpatient_claims rename constraint "OutpatientClaims_beneficiaryId_to_Beneficiaries" to outpatient_claims_bene_id_to_beneficiaries;

-- hsql only
${logic.hsql-only-alter} table public.outpatient_claim_lines add constraint outpatient_claim_lines_pkey primary key (clm_id, clm_line_num);
${logic.hsql-only-alter} table public.outpatient_claims add constraint outpatient_claims_pkey primary key (clm_id);

${logic.hsql-only-alter} table public.outpatient_claim_lines ADD CONSTRAINT outpatient_claim_lines_clm_id_to_outpatient_claims FOREIGN KEY (clm_id) REFERENCES public.outpatient_claims (clm_id);
${logic.hsql-only-alter} table public.outpatient_claims ADD CONSTRAINT outpatient_claims_bene_id_to_beneficiaries FOREIGN KEY (bene_id) REFERENCES public.beneficiaries (bene_id);

-- both psql and hsql support non-primary key index renaming
ALTER INDEX "OutpatientClaims_beneficiaryId_idx" RENAME TO outpatient_claims_bene_id_idx;
