import sqlite3
from faker import Faker

# Connect to the SQLite database
conn = sqlite3.connect('database.db')
cursor = conn.cursor()

# Create the tables
cursor.execute('''
    CREATE TABLE IF NOT EXISTS ProductionData (
        ProductionID TEXT,
        MaterialCode TEXT,
        Temperature INTEGER,
        Pressure INTEGER,
        Speed INTEGER,
        QualityStatus TEXT,
        OperatorID TEXT
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS QualityData (
        ProductionID TEXT,
        MaterialCode TEXT,
        Temperature INTEGER,
        Pressure INTEGER,
        Speed INTEGER,
        QualityStatus TEXT,
        OperatorID TEXT
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS OperatorData (
        OperatorID TEXT,
        FirstName TEXT,
        LastName TEXT,
        Department TEXT,
        Shift TEXT,
        Experience INTEGER,
        Certification TEXT
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS ProductDetails (
        ProductionID TEXT,
        Length INTEGER,
        Weight INTEGER,
        Diameter INTEGER,
        Temperature INTEGER,
        Defects INTEGER,
        Category TEXT
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS QualityAudit (
        AuditID TEXT,
        Result TEXT,
        Remarks TEXT,
        ActionTaken TEXT,
        EmployeeID TEXT
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS ShiftData (
        ShiftDate TEXT,
        Department TEXT,
        PlannedOutput INTEGER,
        ActualOutput INTEGER,
        Shift TEXT
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS EmployeeData (
        EmployeeID TEXT,
        FirstName TEXT,
        LastName TEXT,
        Department TEXT,
        Shift TEXT,
        Experience INTEGER,
        Certification TEXT
    );
''')

# Commit the changes to the database schema
conn.commit()

# Populate the tables with mock data
fake = Faker()

# Generate 1000 entries for the ProductionData table
for _ in range(1000):
    production_id = fake.uuid4()
    material_code = fake.word()
    temperature = fake.random_int(min=0, max=100)
    pressure = fake.random_int(min=0, max=100)
    speed = fake.random_int(min=0, max=100)
    quality_status = fake.random_element(['Good', 'Bad'])
    operator_id = fake.uuid4()

    cursor.execute('''
        INSERT INTO ProductionData (ProductionID, MaterialCode, Temperature, Pressure, Speed, QualityStatus, OperatorID)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    ''', (production_id, material_code, temperature, pressure, speed, quality_status, operator_id))

# Generate mock data for other tables in a similar manner

# Commit the changes and close the connection
conn.commit()
conn.close()

print("Database schema and additional views created successfully.")
