import os
import pandas as pd
import psycopg2
import uuid
from datetime import datetime
from pathlib import Path

# ============================================================
# Configuration
# ============================================================

HISTORICAL_FOLDER = Path("historical-data")

REPORT_FILE = Path(
    "backfill-discrepancy-report.csv"
)

BATCH_SIZE = 500


DB_CONFIG = {
    "host": os.getenv(
        "DB_HOST",
        "localhost"
    ),
    "port": os.getenv(
        "DB_PORT",
        "5432"
    ),
    "database": os.getenv(
        "DB_NAME",
        "virs_db"
    ),
    "user": os.getenv(
        "DB_USERNAME",
        "postgres"
    ),
    "password": os.getenv(
        "DB_PASSWORD",
        "postgres"
    )
}


REQUIRED_COLUMNS = [
    "vendorId",
    "sku",
    "productName",
    "quantity",
    "unitPrice"
]


# ============================================================
# CSV Validation
# ============================================================

def validate_columns(df):
    """
    Validates that all required CSV columns are present.
    """

    missing_columns = [
        column
        for column in REQUIRED_COLUMNS
        if column not in df.columns
    ]

    if missing_columns:
        raise ValueError(
            "Missing required columns: "
            f"{missing_columns}"
        )


def validate_record(row):
    """
    Validates a single inventory record.

    Returns:
        (True, "") when valid
        (False, error_message) when invalid
    """

    errors = []

    vendor_id = row.get("vendorId")
    sku = row.get("sku")
    product_name = row.get("productName")
    quantity = row.get("quantity")
    unit_price = row.get("unitPrice")

    # --------------------------------------------------------
    # vendorId
    # --------------------------------------------------------

    if (
            pd.isna(vendor_id)
            or not str(vendor_id).strip()
    ):
        errors.append(
            "vendorId is required"
        )

    # --------------------------------------------------------
    # SKU
    # --------------------------------------------------------

    if (
            pd.isna(sku)
            or not str(sku).strip()
    ):
        errors.append(
            "sku is required"
        )

    # --------------------------------------------------------
    # Product Name
    # --------------------------------------------------------

    if (
            pd.isna(product_name)
            or not str(product_name).strip()
    ):
        errors.append(
            "productName is required"
        )

    # --------------------------------------------------------
    # Quantity
    # --------------------------------------------------------

    if pd.isna(quantity):

        errors.append(
            "quantity is required"
        )

    else:

        try:

            quantity_value = int(quantity)

            if quantity_value < 0:

                errors.append(
                    "quantity cannot be negative"
                )

        except (
                ValueError,
                TypeError
        ):

            errors.append(
                "quantity must be an integer"
            )

    # --------------------------------------------------------
    # Unit Price
    # --------------------------------------------------------

    if pd.isna(unit_price):

        errors.append(
            "unitPrice is required"
        )

    else:

        try:

            unit_price_value = float(
                unit_price
            )

            if unit_price_value < 0:

                errors.append(
                    "unitPrice cannot be negative"
                )

        except (
                ValueError,
                TypeError
        ):

            errors.append(
                "unitPrice must be numeric"
            )

    if errors:

        return False, "; ".join(errors)

    return True, ""


# ============================================================
# Batch Creation
# ============================================================

def create_batch(
        cursor,
        file_name):

    batch_id = str(
        uuid.uuid4()
    )

    now = datetime.now()

    cursor.execute(
        """
        INSERT INTO batch_execution
        (
            batch_id,
            file_name,
            execution_type,
            source_batch_id,
            status,
            total_records,
            processed_records,
            failed_records,
            batch_size,
            start_time
        )
        VALUES
        (
            %s,
            %s,
            %s,
            %s,
            %s,
            %s,
            %s,
            %s,
            %s,
            %s
        )
        """,
        (
            batch_id,
            file_name,
            "BACKFILL",
            None,
            "RUNNING",
            0,
            0,
            0,
            BATCH_SIZE,
            now
        )
    )

    return batch_id


# ============================================================
# Inventory Insert
# ============================================================

def insert_record(
        cursor,
        batch_id,
        row):

    cursor.execute(
        """
        INSERT INTO vendor_inventory
        (
            vendor_inventory_id,
            batch_id,
            vendor_id,
            sku,
            product_name,
            quantity,
            unit_price,
            reconciliation_status,
            quantity_difference,
            remarks,
            notification_status,
            notification_time,
            upload_time
        )
        VALUES
        (
            %s,
            %s,
            %s,
            %s,
            %s,
            %s,
            %s,
            NULL,
            NULL,
            NULL,
            'PENDING',
            NULL,
            %s
        )
        """,
        (
            str(uuid.uuid4()),
            batch_id,
            str(
                row["vendorId"]
            ).strip(),
            str(
                row["sku"]
            ).strip(),
            str(
                row["productName"]
            ).strip(),
            int(
                row["quantity"]
            ),
            float(
                row["unitPrice"]
            ),
            datetime.now()
        )
    )


# ============================================================
# Batch Update
# ============================================================

def update_batch(
        cursor,
        batch_id,
        total_records,
        processed_records,
        failed_records,
        status):

    cursor.execute(
        """
        UPDATE batch_execution
        SET
            total_records = %s,
            processed_records = %s,
            failed_records = %s,
            status = %s,
            end_time = %s
        WHERE batch_id = %s
        """,
        (
            total_records,
            processed_records,
            failed_records,
            status,
            datetime.now(),
            batch_id
        )
    )


# ============================================================
# Main Backfill
# ============================================================

def main():

    print(
        "===================================="
    )

    print(
        "VIRS Historical Inventory Backfill"
    )

    print(
        "===================================="
    )

    # --------------------------------------------------------
    # Validate historical folder
    # --------------------------------------------------------

    if not HISTORICAL_FOLDER.exists():

        raise FileNotFoundError(
            "Historical folder not found: "
            f"{HISTORICAL_FOLDER}"
        )

    # --------------------------------------------------------
    # Connect to database
    # --------------------------------------------------------

    connection = psycopg2.connect(
        **DB_CONFIG
    )

    report_rows = []

    overall_total = 0
    overall_success = 0
    overall_failed = 0

    try:

        cursor = connection.cursor()

        # ----------------------------------------------------
        # Find CSV files
        # ----------------------------------------------------

        csv_files = sorted(
            file
            for file in HISTORICAL_FOLDER.iterdir()
            if (
                    file.is_file()
                    and file.suffix.lower() == ".csv"
            )
        )

        if not csv_files:

            print(
                "No CSV files found in "
                "historical-data."
            )

            return

        # ----------------------------------------------------
        # Process each CSV
        # ----------------------------------------------------

        for file_path in csv_files:

            file_name = file_path.name

            print()
            print(
                f"Processing: {file_name}"
            )

            batch_id = None

            try:

                # --------------------------------------------
                # Read CSV
                # --------------------------------------------

                df = pd.read_csv(
                    file_path,
                    dtype={
                        "vendorId": "string",
                        "sku": "string",
                        "productName": "string"
                    }
                )

                # --------------------------------------------
                # Clean column names
                # --------------------------------------------

                df.columns = [
                    str(column).strip()
                    for column in df.columns
                ]

                # --------------------------------------------
                # Validate columns
                # --------------------------------------------

                validate_columns(df)

                # --------------------------------------------
                # Create BACKFILL batch
                # --------------------------------------------

                batch_id = create_batch(
                    cursor,
                    file_name
                )

                total_records = len(df)

                processed_records = 0

                failed_records = 0

                seen_skus = set()

                # --------------------------------------------
                # Process records
                # --------------------------------------------

                for index, row in df.iterrows():

                    row_number = index + 2

                    # ----------------------------------------
                    # Validate record
                    # ----------------------------------------

                    valid, error = validate_record(
                        row
                    )

                    if not valid:

                        failed_records += 1

                        report_rows.append(
                            {
                                "file_name": file_name,
                                "row_number": row_number,
                                "sku": row.get("sku"),
                                "status": "FAILED",
                                "reason": error
                            }
                        )

                        continue

                    # ----------------------------------------
                    # Duplicate SKU validation
                    # ----------------------------------------

                    sku = str(
                        row["sku"]
                    ).strip()

                    if sku in seen_skus:

                        failed_records += 1

                        report_rows.append(
                            {
                                "file_name": file_name,
                                "row_number": row_number,
                                "sku": sku,
                                "status": "FAILED",
                                "reason":
                                    "Duplicate SKU in same file"
                            }
                        )

                        continue

                    seen_skus.add(sku)

                    # ----------------------------------------
                    # Insert inventory record
                    # ----------------------------------------

                    insert_record(
                        cursor,
                        batch_id,
                        row
                    )

                    processed_records += 1

                # --------------------------------------------
                # Determine batch status
                # --------------------------------------------

                if failed_records == 0:

                    status = "COMPLETED"

                else:

                    status = "FAILED"

                # --------------------------------------------
                # Update batch
                # --------------------------------------------

                update_batch(
                    cursor,
                    batch_id,
                    total_records,
                    processed_records,
                    failed_records,
                    status
                )

                # --------------------------------------------
                # Commit current file
                # --------------------------------------------

                connection.commit()

                # --------------------------------------------
                # Update overall statistics
                # --------------------------------------------

                overall_total += total_records

                overall_success += processed_records

                overall_failed += failed_records

                # --------------------------------------------
                # Console output
                # --------------------------------------------

                print(
                    f"Batch ID : {batch_id}"
                )

                print(
                    f"Total    : {total_records}"
                )

                print(
                    f"Processed: {processed_records}"
                )

                print(
                    f"Failed   : {failed_records}"
                )

                print(
                    f"Status   : {status}"
                )

            except Exception as ex:

                # --------------------------------------------
                # Rollback current file
                # --------------------------------------------

                connection.rollback()

                overall_failed += 1

                report_rows.append(
                    {
                        "file_name": file_name,
                        "row_number": "",
                        "sku": "",
                        "status": "FAILED",
                        "reason": str(ex)
                    }
                )

                print(
                    f"FAILED - {file_name}: {ex}"
                )

        # ----------------------------------------------------
        # Generate discrepancy / validation report
        # ----------------------------------------------------

        if report_rows:

            report_df = pd.DataFrame(
                report_rows,
                columns=[
                    "file_name",
                    "row_number",
                    "sku",
                    "status",
                    "reason"
                ]
            )

            report_df.to_csv(
                REPORT_FILE,
                index=False
            )

            print()

            print(
                "Validation report generated: "
                f"{REPORT_FILE}"
            )

        # ----------------------------------------------------
        # Final summary
        # ----------------------------------------------------

        print()

        print(
            "===================================="
        )

        print(
            "Backfill Completed"
        )

        print(
            "===================================="
        )

        print(
            f"Total records   : {overall_total}"
        )

        print(
            f"Processed       : {overall_success}"
        )

        print(
            f"Failed          : {overall_failed}"
        )

    finally:

        connection.close()


# ============================================================
# Application Entry Point
# ============================================================

if __name__ == "__main__":

    main()