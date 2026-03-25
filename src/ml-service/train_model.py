import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, roc_auc_score
import joblib

print("Carregando dataset...")
df = pd.read_csv("creditcard.csv")

print(f"Total de transacoes: {len(df)}")
print(f"Fraudes: {df['Class'].sum()} ({df['Class'].mean()*100:.2f}%)")

X = df.drop(columns=["Class", "Time"])
y = df["Class"]

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

print("\nTreinando Random Forest...")
model = RandomForestClassifier(
    n_estimators=100,
    max_depth=10,
    random_state=42,
    n_jobs=-1,
    class_weight="balanced"
)
model.fit(X_train, y_train)

print("\n=== METRICAS DO MODELO ===")
y_pred = model.predict(X_test)
y_prob = model.predict_proba(X_test)[:, 1]
print(classification_report(y_test, y_pred, target_names=["Legitima", "Fraude"]))
try:
    auc = roc_auc_score(y_test, y_prob)
    print(f"ROC-AUC Score: {auc:.4f}")
except Exception as e:
    print(f"ROC-AUC Score: nao calculado ({e})")

joblib.dump(model, "fraud_model.pkl")
print("\nModelo salvo em fraud_model.pkl")