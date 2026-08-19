import os
import uuid
from datetime import datetime

import pandas as pd
import psycopg2

HISTORICAL_FOLDER = "historical-data"

DB_CONFIG = {
    "host": "localhost",
    "port": "5432",
    "database": "virs_db",
    "user": "postgres",
    "password": "postgres"
}

required_columns = [
    "vendorId",
    "sku",
    "productName",
    "quantity",
    "unitPrice"
]

total_records = 0

connection = psycopg2.connect(**DB_CONFIG)

cursor = connection.cursor()

print("====================================")
print("VIRS Historical Inventory Backfill")
print("====================================")

batch_id = str(uuid.uuid4())

cursor.execute(
    """
    INSERT INTO batch_execution
    (
        batch_id,
        file_name,
        execution_type,
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
        %s
    )
    """,
    (
        batch_id,
        "historical-backfill",
        "INITIAL",
        "COMPLETED",
        0,
        0,
        0,
        500,
        datetime.now()
    )
)

connection.commit()

for file_name in os.listdir(HISTORICAL_FOLDER):

    if not file_name.endswith(".csv"):
        continue

    file_path = os.path.join(
        HISTORICAL_FOLDER,
        file_name
    )

    print(f"\nProcessing : {file_name}")

    try:

        df = pd.read_csv(file_path)

        missing_columns = [
            column
            for column in required_columns
            if column not in df.columns
        ]

        if missing_columns:

            print(
                f"FAILED - Missing columns : "
                f"{missing_columns}"
            )

            continue

        processed_count = 0

        for _, row in df.iterrows():

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
                    %s,
                    NULL,
                    %s
                )
                """,
                (
                    str(uuid.uuid4()),
                    batch_id,
                    row["vendorId"],
                    row["sku"],
                    row["productName"],
                    int(row["quantity"]),
                    row["unitPrice"],
                    "PENDING",
                    datetime.now()
                )
            )

            processed_count += 1

        connection.commit()

        total_records += processed_count

        print(
            f"SUCCESS - Loaded {processed_count} records"
        )

    except Exception as ex:

        connection.rollback()

        print(
            f"FAILED - {file_name}"
        )

        print(str(ex))

cursor.execute(
    """
    UPDATE batch_execution
    SET
        total_records = %s,
        processed_records = %s,
        end_time = %s
    WHERE batch_id = %s
    """,
    (
        total_records,
        total_records,
        datetime.now(),
        batch_id
    )
)

connection.commit()

print("\n====================================")
print("Backfill Completed")
print("====================================")
print(
    f"Total Records Processed : {total_records}"
)

cursor.close()
connection.close()