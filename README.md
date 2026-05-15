# Amping

> A gamified AI companion turns TB treatment into a rewarding quest with secure video verification.

## 📖 Overview

Tuberculosis (TB) remains a dominant public health crisis, worsened by a high rate of treatment non-compliance. Interrupted regimens are the primary driver for multidrug-resistant tuberculosis (MDR-TB), with non-adherent patients being 11.5 times more likely to develop resistant strains. While traditional Directly Observed Treatment (DOT) is the standard, it is hindered by social stigma, travel costs, and adherence decay over the 6-to-9-month treatment course. Current TB monitoring in local settings relies on intermittent human supervision or basic digital logs that lack long-term engagement hooks.

**Amping** is a mobile-based health platform that addresses this gap by transforming the rigorous TB treatment schedule into a rewarding quest. It combines asynchronous Video-Observed Therapy (VOT) with a conversational AI companion to sustain patient engagement across the full treatment course. 

## ✨ Key Features

* **Conversational AI Companion ("Gabby"):** An AI agent that engages patients daily through a structured three-stage sequence: mood & symptoms check, motivational exchange, and then dose recording. Gabby is designed as a companion rather than a clinical alarm.
* **Gamified Quest System:** Each completed daily session awards XP, updates streak counters, and unlocks achievement badges. The system uses cooperative, progress-based mechanics to counter notification burnout.
* **Age-Adaptive Engagement:** Delivers age-appropriate gamification mechanics and AI companion interaction styles to prevent motivational mismatch.
* **Automated Video Verification:** Upon completing the conversational sequence, Gabby issues a tool-call that invokes the device's native camera API to record the dose, requiring no manual input from the client-side.
* **Secure Uploads:** The recorded MP4 is transmitted to the Django backend via TLS encryption.
* **Attending Physician Dashboard:** A Django-based web dashboard provides authorized healthcare workers with role-restricted access to uploaded dose videos for asynchronous clinical review. It also includes actionable analytics to detect emerging non-adherence patterns.

## ⚙️ System Workflow

A single daily session follows this sequence:
1.  **Patient Input:** The patient speaks to Gabby via the mobile app, and voice input is sent through a Speech-to-Text (STT) engine to an LLM inference request.
2.  **AI Processing:** A serverless GPU processes the request and returns either a natural language response or a recording tool-call.
3.  **Recording & Gamification:** The app outputs audio via Text-to-Speech (TTS), triggers the camera to record an MP4, awards XP, and updates the patient's streak.
4.  **Secure Storage:** The video is transmitted via TLS-encrypted upload to the Django backend and cloud storage.
5.  **Clinical Review:** Providers access the dashboard for role-authenticated playback to verify compliance.
6.  **Risk Engine:** Engagement and compliance data inform adherence risk stratification, triggering alerts for healthcare workers if needed.

## 🛠️ Development Methodology

The project follows a **Hybrid Spiral-Agile** methodology. Because Amping handles sensitive patient health data, risk mitigation is treated as a first-class development concern. 
* The **Spiral model** provides a structured risk analysis gate before any engineering work begins on each system layer. 
* **Agile sprints** within each spiral cycle maintain iterative, feedback-driven development.

The development is organized into four primary spiral cycles:
1.  **Spiral 1:** Security Architecture & Backend Foundation
2.  **Spiral 2:** AI Companion Core & Automated Video Verification
3.  **Spiral 3:** Gamification Layer & Purpose-driven Mobile Frontend
4.  **Spiral 4:** Dashboard Integration & End-to-End System

## 👥 Team

**Team Code:** 2526-sem2-cs342-08

**Members:**
* Rafael A. Mendoza
* Raymond Gerard Y. Tio
* Gil Florenz J. Sastre
* Kelvin Pehrson P. Kierulf
* John Zillion C. Reyes