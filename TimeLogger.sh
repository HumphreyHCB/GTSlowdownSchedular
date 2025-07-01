#!/usr/bin/env bash
###############################################################################
# run_godrunner.sh – launch GodRunner and record timing information
# Usage:  chmod +x run_godrunner.sh
#         nohup ./run_godrunner.sh &   # runs in the background, survives logout
###############################################################################

# Create a unique logfile for this invocation (change the path if you like)
LOGFILE="godrunner_$(date '+%Y%m%d_%H%M%S').log"

# Human-readable start time
echo "=== Start: $(date '+%Y-%m-%d %H:%M:%S %Z') ===" | tee -a "$LOGFILE"

# Epoch seconds for duration calculation
START_EPOCH=$(date +%s)

# ---------- run your Java program ----------
java -cp .:GTResources/org.json-1.6-20240205.jar GodRunner \
     >>"$LOGFILE" 2>&1
# ---------- finished -----------------------

END_EPOCH=$(date +%s)
echo "=== End:   $(date '+%Y-%m-%d %H:%M:%S %Z') ===" | tee -a "$LOGFILE"

# Calculate total runtime (HH:MM:SS)
DURATION=$(( END_EPOCH - START_EPOCH ))
printf '=== Duration: %02d:%02d:%02d ===\n' \
        $(( DURATION/3600 )) $(( (DURATION%3600)/60 )) $(( DURATION%60 )) \
        | tee -a "$LOGFILE"
