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
-- MedicareBeneficiaryIdHistory to medicare_beneficiaryid_history
--
alter table public."MedicareBeneficiaryIdHistory" rename to medicare_beneficiaryid_history;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "medicareBeneficiaryIdKey" ${logic.rename-to} bene_mbi_id;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "beneficiaryId" ${logic.rename-to} bene_id;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} lastupdated ${logic.rename-to} last_updated;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "claimAccountNumber" ${logic.rename-to} bene_clm_acnt_num;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "beneficiaryIdCode" ${logic.rename-to} bene_ident_cd;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiCrntRecIndId" ${logic.rename-to} bene_crnt_rec_ind_id;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiSequenceNumber" ${logic.rename-to} mbi_sqnc_num;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "medicareBeneficiaryId" ${logic.rename-to} mbi_num;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiEffectiveDate" ${logic.rename-to} mbi_efctv_bgn_dt;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiEndDate" ${logic.rename-to} mbi_efctv_end_dt;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiEffectiveReasonCode" ${logic.rename-to} mbi_bgn_rsn_cd;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiEndReasonCode" ${logic.rename-to} mbi_end_rsn_cd;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiCardRequestDate" ${logic.rename-to} mbi_card_rqst_dt;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiAddUser" ${logic.rename-to} creat_user_id;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiAddDate" ${logic.rename-to} creat_ts;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiUpdateUser" ${logic.rename-to} updt_user_id;
alter table public.medicare_beneficiaryid_history ${logic.alter-rename-column} "mbiUpdateDate" ${logic.rename-to} updt_ts;
--
-- MedicareBeneficiaryIdHistoryInvalidBeneficiaries to medicare_beneficiaryid_history_invalid_beneficiaries
--
alter table public."MedicareBeneficiaryIdHistoryInvalidBeneficiaries" rename to medicare_beneficiaryid_history_invalid_beneficiaries;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "medicareBeneficiaryIdKey" ${logic.rename-to} bene_mbi_id;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "beneficiaryId" ${logic.rename-to} bene_id;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "claimAccountNumber" ${logic.rename-to} bene_clm_acnt_num;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "beneficiaryIdCode" ${logic.rename-to} bene_ident_cd;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiCrntRecIndId" ${logic.rename-to} bene_crnt_rec_ind_id;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiSequenceNumber" ${logic.rename-to} mbi_sqnc_num;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "medicareBeneficiaryId" ${logic.rename-to} mbi_num;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiEffectiveDate" ${logic.rename-to} mbi_efctv_bgn_dt;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiEndDate" ${logic.rename-to} mbi_efctv_end_dt;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiEffectiveReasonCode" ${logic.rename-to} mbi_bgn_rsn_cd;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiEndReasonCode" ${logic.rename-to} mbi_end_rsn_cd;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiCardRequestDate" ${logic.rename-to} mbi_card_rqst_dt;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiAddUser" ${logic.rename-to} creat_user_id;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiAddDate" ${logic.rename-to} creat_ts;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiUpdateUser" ${logic.rename-to} updt_user_id;
alter table public.medicare_beneficiaryid_history_invalid_beneficiaries ${logic.alter-rename-column} "mbiUpdateDate" ${logic.rename-to} updt_ts;

-- psql only
${logic.psql-only-alter} index if exists public."MedicareBeneficiaryIdHistoryInvalidBeneficiaries_pkey" rename to medicare_beneficiaryid_history_invalid_beneficiaries_pkey;
${logic.psql-only-alter} index if exists public."MedicareBeneficiaryIdHistory_pkey" rename to medicare_beneficiaryid_history_pkey;

${logic.psql-only-alter} table public.medicare_beneficiaryid_history rename constraint "MedicareBeneficiaryIdHistory_beneficiaryId_to_Beneficiary" to medicare_beneficiaryid_history_bene_id_to_beneficiaries;

-- hsql only
${logic.hsql-only-alter} table public.medicare_beneficiaryid_history_invalid_beneficiaries add constraint medicare_beneficiaryid_history_invalid_beneficiaries_pkey primary key (bene_mbi_id); 
${logic.hsql-only-alter} table public.medicare_beneficiaryid_history add constraint medicare_beneficiaryid_history_pkey primary key (bene_mbi_id);   

${logic.hsql-only-alter} table public.medicare_beneficiaryid_history ADD CONSTRAINT medicare_beneficiaryid_history_bene_id_to_beneficiaries FOREIGN KEY (bene_id) REFERENCES public.beneficiaries (bene_id);

-- both psql and hsql support non-primary key index renaming
ALTER INDEX "MedicareBeneficiaryIdHistory_beneficiaryId_idx" RENAME TO medicare_beneficiaryid_history_bene_id_idx;
