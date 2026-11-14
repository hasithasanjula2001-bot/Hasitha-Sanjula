package com.example.data

object ComplianceSeedData {

    fun sanctions(): List<SanctionEntryEntity> = listOf(
        SanctionEntryEntity(
            entityName = "Severny Port Maritime Logistics LLC",
            program = SanctionProgram.OFAC_SDN.name,
            countryIso = "RU",
            countryName = "Russia",
            entityType = "ENTITY",
            riskTier = RiskTier.CRITICAL.name,
            updatedDate = "2026-10-18",
            keywords = "severny port,severny maritime,kaliningrad logistics",
            notes = "Designated under E.O. 14024 for sanctioned dual-use maritime transport."
        ),
        SanctionEntryEntity(
            entityName = "Caspian Shadow Tanker Fleet (M/V Astra-9)",
            program = SanctionProgram.EU_CONSOLIDATED.name,
            countryIso = "IR",
            countryName = "Iran",
            entityType = "VESSEL",
            riskTier = RiskTier.CRITICAL.name,
            updatedDate = "2026-11-02",
            keywords = "astra-9,caspian shadow,bandar abbas,imo 9418821",
            notes = "EU Council Reg. 2024/1842 — AIS-spoofing vessel prohibited from EU/USD clearing."
        ),
        SanctionEntryEntity(
            entityName = "Pyongyang Taedong Trade Bank",
            program = SanctionProgram.UN_SC.name,
            countryIso = "KP",
            countryName = "North Korea",
            entityType = "ENTITY",
            riskTier = RiskTier.CRITICAL.name,
            updatedDate = "2026-09-29",
            keywords = "taedong,pyongyang,dprk,koryo credit",
            notes = "UNSCR 1718 / 2375 asset freeze & correspondent banking prohibition."
        ),
        SanctionEntryEntity(
            entityName = "Al-Sham Petrochemical Trading Co.",
            program = SanctionProgram.OFAC_SDN.name,
            countryIso = "SY",
            countryName = "Syria",
            entityType = "ENTITY",
            riskTier = RiskTier.CRITICAL.name,
            updatedDate = "2026-10-04",
            keywords = "al-sham,tartus,latakia,syria",
            notes = "OFAC Syria Sanctions Regulations — blocked petroleum & industrial trade."
        ),
        SanctionEntryEntity(
            entityName = "Minsk Precision Optics Plant (MPOP)",
            program = SanctionProgram.EU_CONSOLIDATED.name,
            countryIso = "BY",
            countryName = "Belarus",
            entityType = "ENTITY",
            riskTier = RiskTier.HIGH.name,
            updatedDate = "2026-10-27",
            keywords = "minsk precision,mpop,belarus optics",
            notes = "Designated for military-industrial optical & laser rangefinder supply."
        ),
        SanctionEntryEntity(
            entityName = "Bosphorus Transit Hub — High-Risk Diversion Corridor",
            program = SanctionProgram.OFAC_SDN.name,
            countryIso = "TR",
            countryName = "Turkey",
            entityType = "PORT",
            riskTier = RiskTier.ELEVATED.name,
            updatedDate = "2026-11-05",
            keywords = "free zone diversion,common high priority,chpl",
            notes = "Enhanced due diligence required for BIS Common High Priority List (CHPL) re-exports."
        ),
        SanctionEntryEntity(
            entityName = "Red Sea Dual-Use Intermediary FZE",
            program = SanctionProgram.UN_SC.name,
            countryIso = "YE",
            countryName = "Yemen",
            entityType = "ENTITY",
            riskTier = RiskTier.HIGH.name,
            updatedDate = "2026-10-14",
            keywords = "hodeidah,red sea intermediary,al-hudaydah",
            notes = "UN Panel of Experts maritime arms & propulsion component embargo alert."
        ),
        SanctionEntryEntity(
            entityName = "Myanmar Gems & Timber State Enterprise",
            program = SanctionProgram.EU_CONSOLIDATED.name,
            countryIso = "MM",
            countryName = "Myanmar",
            entityType = "ENTITY",
            riskTier = RiskTier.HIGH.name,
            updatedDate = "2026-09-21",
            keywords = "myanmar timber,mge,yangon state enterprise",
            notes = "EU & OFAC state-owned enterprise blocking order."
        )
    )

    fun rules(): List<ComplianceRuleEntity> = listOf(
        ComplianceRuleEntity(
            ruleCode = "UCP600-Art14",
            category = RuleCategory.UCP_600.name,
            title = "Standard for Examination of Documents (5 Banking Days)",
            summary = "Banks must examine presentations on their face within 5 banking days; goods descriptions on invoices must correspond with the credit.",
            severity = "HIGH",
            effectiveDate = "UCP 600 (2026 Guidance)",
            hsPrefixes = "",
            keywords = "invoice mismatch,5 banking days,goods description,correspond"
        ),
        ComplianceRuleEntity(
            ruleCode = "UCP600-Art18",
            category = RuleCategory.UCP_600.name,
            title = "Commercial Invoice Currency & Tolerance Rules",
            summary = "Invoice must be issued by beneficiary to applicant in the exact LC currency; excess quantity tolerance +/-5% allowed unless prohibited.",
            severity = "HIGH",
            effectiveDate = "UCP 600 Art. 18 & 30",
            hsPrefixes = "",
            keywords = "currency mismatch,tolerance,commercial invoice,overdrawn"
        ),
        ComplianceRuleEntity(
            ruleCode = "UCP600-Art20",
            category = RuleCategory.UCP_600.name,
            title = "Bill of Lading — Clean On-Board & Transshipment",
            summary = "B/L must show shipped on board a named vessel at Field :44E: port, signed by carrier/master, and contain no claused/unclean notations.",
            severity = "CRITICAL",
            effectiveDate = "UCP 600 Art. 20",
            hsPrefixes = "",
            keywords = "bill of lading,on board,claused,transshipment,port of loading"
        ),
        ComplianceRuleEntity(
            ruleCode = "ISBP821-B12",
            category = RuleCategory.ISBP_821.name,
            title = "Sanctions Clauses & Non-Documentary Conditions",
            summary = "Updated ICC guidance warns against vague sanctions clauses creating non-documentary ambiguity; verify explicit OFAC/EU/UN scope.",
            severity = "HIGH",
            effectiveDate = "ISBP 821 (2025/2026)",
            hsPrefixes = "",
            keywords = "sanctions clause,non-documentary,boycott,compliance clause"
        ),
        ComplianceRuleEntity(
            ruleCode = "DUAL-USE-3A001",
            category = RuleCategory.DUAL_USE.name,
            title = "Electronic Components, Semiconductors & FPGAs",
            summary = "Wassenaar / BIS CHPL Tier-1 integrated circuits, radiation-hardened chips, and high-frequency RF modules require end-user license verification.",
            severity = "CRITICAL",
            effectiveDate = "EU Reg 2021/821 (2026 Update)",
            hsPrefixes = "8542,8541,8517",
            keywords = "semiconductor,integrated circuit,fpga,microprocessor,wafer,rf transceiver"
        ),
        ComplianceRuleEntity(
            ruleCode = "DUAL-USE-5A002",
            category = RuleCategory.DUAL_USE.name,
            title = "Information Security & Cryptographic Hardware",
            summary = "Equipment employing symmetric algorithms >56-bit or quantum key distribution requires dual-use export declaration.",
            severity = "HIGH",
            effectiveDate = "Wassenaar Cat 5 Part 2",
            hsPrefixes = "8517,8471,8543",
            keywords = "encryption,cryptographic,hardware security module,hsm,secure router"
        ),
        ComplianceRuleEntity(
            ruleCode = "DUAL-USE-2B001",
            category = RuleCategory.DUAL_USE.name,
            title = "5-Axis CNC Precision Machine Tools",
            summary = "Numerical control machine tools with positioning accuracy <= 3.0 micrometers fall under Wassenaar 2B001 / CHPL Tier-3.",
            severity = "CRITICAL",
            effectiveDate = "BIS EAR 2B001 (2026)",
            hsPrefixes = "8457,8458,8459,8460",
            keywords = "cnc,5-axis,machining center,precision lathe,spindle"
        ),
        ComplianceRuleEntity(
            ruleCode = "DUAL-USE-6A003",
            category = RuleCategory.DUAL_USE.name,
            title = "Thermal Imaging Cameras & Optical Sensors",
            summary = "Uncooled microbolometer focal plane arrays, LiDAR, and night-vision optics require military/civilian end-use certification.",
            severity = "CRITICAL",
            effectiveDate = "EU Dual-Use 6A003",
            hsPrefixes = "9005,9013,9015,8525",
            keywords = "thermal camera,infrared,lidar,optical sensor,microbolometer"
        ),
        ComplianceRuleEntity(
            ruleCode = "DUAL-USE-1C350",
            category = RuleCategory.DUAL_USE.name,
            title = "Controlled Chemical Precursors & Corrosion Equipment",
            summary = "Dual-use industrial chemical precursors, fluorinated compounds, and hastelloy reactors screened under Australia Group rules.",
            severity = "HIGH",
            effectiveDate = "Australia Group 2026",
            hsPrefixes = "2812,2921,2930,8419",
            keywords = "precursor,thionyl,fluoride,hastelloy,chemical reactor"
        ),
        ComplianceRuleEntity(
            ruleCode = "SANCTION-MARITIME-01",
            category = RuleCategory.SANCTIONS_POLICY.name,
            title = "AIS Gap & STS Transfer Screening Mandate",
            summary = "Bills of Lading and Charter Parties must certify no ship-to-ship (STS) transfer in high-risk zones and continuous AIS transponder tracking.",
            severity = "HIGH",
            effectiveDate = "OFAC Maritime Advisory",
            hsPrefixes = "",
            keywords = "ship-to-ship,ais,dark fleet,charter party,maritime"
        )
    )

    fun exposureLimits(): List<ExposureLimitEntity> = listOf(
        ExposureLimitEntity(
            scope = LimitScope.SINGLE_TX.name,
            targetName = "Single LC Transaction Cap",
            targetIdentifier = "GLOBAL",
            maxCapUsd = 5_000_000.0,
            baselineCommittedUsd = 0.0,
            alertThresholdPercent = 80,
            isActive = true
        ),
        ExposureLimitEntity(
            scope = LimitScope.DUAL_USE_CAP.name,
            targetName = "Dual-Use Controlled Goods Aggregate",
            targetIdentifier = "DUAL_USE",
            maxCapUsd = 3_500_000.0,
            baselineCommittedUsd = 850_000.0,
            alertThresholdPercent = 70,
            isActive = true
        ),
        ExposureLimitEntity(
            scope = LimitScope.COUNTRY.name,
            targetName = "Germany",
            targetIdentifier = "DE",
            maxCapUsd = 12_000_000.0,
            baselineCommittedUsd = 4_200_000.0,
            alertThresholdPercent = 75,
            isActive = true
        ),
        ExposureLimitEntity(
            scope = LimitScope.COUNTRY.name,
            targetName = "Singapore",
            targetIdentifier = "SG",
            maxCapUsd = 10_000_000.0,
            baselineCommittedUsd = 3_100_000.0,
            alertThresholdPercent = 75,
            isActive = true
        ),
        ExposureLimitEntity(
            scope = LimitScope.COUNTRY.name,
            targetName = "Turkey",
            targetIdentifier = "TR",
            maxCapUsd = 4_000_000.0,
            baselineCommittedUsd = 2_100_000.0,
            alertThresholdPercent = 70,
            isActive = true
        ),
        ExposureLimitEntity(
            scope = LimitScope.ISSUING_BANK.name,
            targetName = "Deutsche Handelsbank AG",
            targetIdentifier = "DEUTDEFF",
            maxCapUsd = 8_000_000.0,
            baselineCommittedUsd = 2_800_000.0,
            alertThresholdPercent = 75,
            isActive = true
        ),
        ExposureLimitEntity(
            scope = LimitScope.ISSUING_BANK.name,
            targetName = "Meridian Pacific Bank",
            targetIdentifier = "MPBCSGSG",
            maxCapUsd = 6_500_000.0,
            baselineCommittedUsd = 1_900_000.0,
            alertThresholdPercent = 75,
            isActive = true
        )
    )

    fun initialVerifiedLcs(): List<VerifiedLcEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            VerifiedLcEntity(
                lcNumber = "LC-SG-2026-88412",
                lcType = "IRREVOCABLE CONFIRMED",
                applicantName = "Nordic MedTech GmbH",
                applicantCountry = "Germany",
                beneficiaryName = "Apex BioDiagnostics Pte Ltd",
                beneficiaryCountry = "Singapore",
                issuingBank = "Deutsche Handelsbank AG",
                issuingBankBic = "DEUTDEFFXXX",
                advisingBank = "Meridian Pacific Bank",
                advisingBankBic = "MPBCSGSGXXX",
                currency = "USD",
                amountUsd = 1_450_000.0,
                expiryDate = "2027-02-15",
                latestShipmentDate = "2027-01-20",
                portOfLoading = "Port of Singapore (SGSIN)",
                portOfDischarge = "Hamburg Eurogate (DEHAM)",
                goodsDescription = "Automated clinical hematology analyzers and sterile reagent cartridges, CIF Hamburg",
                hsCode = "9027.89",
                documentName = "MT700_LC_SG_88412.pdf",
                mimeType = "application/pdf",
                sourceUri = "",
                sha256Hash = "a8f4c91e30b27d5f94c612a871b3e0d419f7c3b82d6a91e4f05c27b8d19e43a2",
                documentTypesJson = ComplianceJsonCodec.encodeStringList(
                    listOf("SWIFT MT700", "Commercial Invoice", "Clean On-Board B/L")
                ),
                riskScore = 12,
                status = LcStatus.CLEARED.name,
                dualUseFlagged = false,
                dualUseEccn = "NONE",
                dualUseRationale = "Medical diagnostic equipment under HS 9027.89 is non-controlled civilian healthcare hardware.",
                sanctionsFlagged = false,
                sanctionsMatchesJson = "[]",
                discrepanciesJson = "[]",
                limitAlertsJson = "[]",
                aiSummary = "All SWIFT MT700 fields, UCP 600 documentary timelines, and maritime corridor checks passed with low risk.",
                verifiedAtMillis = now - 3_600_000L * 6
            ),
            VerifiedLcEntity(
                lcNumber = "LC-TR-2026-50931",
                lcType = "IRREVOCABLE",
                applicantName = "Anatolia Industrial Robotics AS",
                applicantCountry = "Turkey",
                beneficiaryName = "Kansai Precision Mechanics Co.",
                beneficiaryCountry = "Japan",
                issuingBank = "Bosphorus Commercial Bank",
                issuingBankBic = "BCOMTRISXXX",
                advisingBank = "Mizuho Trade Finance Tokyo",
                advisingBankBic = "MHCBJPJTXXX",
                currency = "USD",
                amountUsd = 1_180_000.0,
                expiryDate = "2027-01-30",
                latestShipmentDate = "2027-01-10",
                portOfLoading = "Port of Yokohama (JPYOK)",
                portOfDischarge = "Mersin Free Zone (TRMER)",
                goodsDescription = "5-Axis CNC Precision Machining Centers with 2.0-micron repeatability and controller boards",
                hsCode = "8457.10",
                documentName = "INV_CNC_5AXIS_50931.jpg",
                mimeType = "image/jpeg",
                sourceUri = "",
                sha256Hash = "4e91b208f7c3a16d52e904b817c6f3d092a5e8c1b7f40d62e39a1c8b5d0f2719",
                documentTypesJson = ComplianceJsonCodec.encodeStringList(
                    listOf("SWIFT MT700", "Proforma Invoice")
                ),
                riskScore = 62,
                status = LcStatus.REVIEW_REQUIRED.name,
                dualUseFlagged = true,
                dualUseEccn = "DUAL-USE-2B001",
                dualUseRationale = "5-Axis CNC machine tools (HS 8457.10) with <=3.0 micron positioning fall under Wassenaar 2B001 / BIS CHPL Tier-3.",
                sanctionsFlagged = false,
                sanctionsMatchesJson = "[]",
                discrepanciesJson = ComplianceJsonCodec.encodeDiscrepancies(
                    listOf(
                        LcDiscrepancy(
                            code = "ISBP821-B12",
                            severity = "HIGH",
                            title = "Free Zone end-user certificate absent",
                            detail = "Destination is Mersin Free Zone without an End-User Undertaking (EUU) for CHPL Tier-3 CNC equipment.",
                            recommendation = "Obtain signed End-User Statement & export license prior to LC confirmation."
                        )
                    )
                ),
                limitAlertsJson = ComplianceJsonCodec.encodeStringList(
                    listOf("WARNING [Turkey]: Utilization reaches 82% of $4.00M cap.")
                ),
                aiSummary = "Dual-use 5-Axis CNC equipment destined for a high-scrutiny transshipment free zone requires export license verification.",
                verifiedAtMillis = now - 3_600_000L * 28
            ),
            VerifiedLcEntity(
                lcNumber = "LC-BLK-2026-11904",
                lcType = "IRREVOCABLE TRANSFERABLE",
                applicantName = "Severny Port Maritime Logistics LLC",
                applicantCountry = "Russia",
                beneficiaryName = "Red Sea Dual-Use Intermediary FZE",
                beneficiaryCountry = "Yemen",
                issuingBank = "Eurasian Trade Credit Bank",
                issuingBankBic = "ETCBXXXX",
                advisingBank = "Gulf Trade Clearing",
                advisingBankBic = "GTCLAEADXXX",
                currency = "USD",
                amountUsd = 2_350_000.0,
                expiryDate = "2026-12-20",
                latestShipmentDate = "2026-12-28",
                portOfLoading = "Bandar Abbas (IRBND)",
                portOfDischarge = "Al-Hudaydah (YEHOD)",
                goodsDescription = "Uncooled thermal imaging sensor arrays and cryptographic RF transceivers",
                hsCode = "8542.31",
                documentName = "MT700_DRAFT_11904.txt",
                mimeType = "text/plain",
                sourceUri = "",
                sha256Hash = "9c3d7e14a6b802f51d49c7e208b3a5f61d9c4e70b28a3f51c6e90d4b7a2f18c3",
                documentTypesJson = ComplianceJsonCodec.encodeStringList(
                    listOf("SWIFT MT700", "Charter Party Draft")
                ),
                riskScore = 98,
                status = LcStatus.BLOCKED.name,
                dualUseFlagged = true,
                dualUseEccn = "3A001 / 6A003 / 5A002",
                dualUseRationale = "Thermal imaging arrays and cryptographic RF transceivers are strictly controlled dual-use technologies.",
                sanctionsFlagged = true,
                sanctionsMatchesJson = ComplianceJsonCodec.encodeStringList(
                    listOf(
                        "[OFAC_SDN] Severny Port Maritime Logistics LLC (RU) — CRITICAL risk: Designated under E.O. 14024.",
                        "[UN_SC] Red Sea Dual-Use Intermediary FZE (YE) — HIGH risk: Maritime arms embargo alert."
                    )
                ),
                discrepanciesJson = ComplianceJsonCodec.encodeDiscrepancies(
                    listOf(
                        LcDiscrepancy(
                            code = "UCP600-Art6",
                            severity = "CRITICAL",
                            title = "Expiry precedes latest shipment date",
                            detail = "LC expiry (2026-12-20) is earlier than latest shipment (2026-12-28).",
                            recommendation = "Reject presentation and escalate to MLRO / Sanctions Officer immediately."
                        )
                    )
                ),
                limitAlertsJson = "[]",
                aiSummary = "CRITICAL BLOCK: Multiple OFAC/UN sanctions hits, dual-use military electronics, and invalid MT700 date sequence.",
                verifiedAtMillis = now - 3_600_000L * 52
            )
        )
    }
}
