# K3s 7-Phase 실전 학습 로드맵

## 프로젝트 개요
- **목표**: 홈랩 환경에서 K3s 클러스터 구축 및 운영 능력 습득
- **기간**: 2주 (14일)
- **구성**: 7개 Phase로 점진적 학습

---

## Phase 1: Kubernetes 첫 걸음 (Day 1-2)

### 학습 목표
- Kubernetes 기본 개념 이해
- kubectl 명령어 익숙해지기

### Day 1: 개념과 구조
```
- Kubernetes 아키텍처
- Master/Worker Node 이해
- Pod, Node, Namespace 개념
```

### Day 2: 실습
```bash
# Namespace 생성
kubectl apply -f phase1-first-steps/01-namespace.yaml

# 첫 Pod 실행
kubectl apply -f phase1-first-steps/02-pod.yaml

# 기본 명령어 연습
kubectl get pods -A
kubectl describe pod <name> -n test-namespace
kubectl logs <pod-name> -n test-namespace
```

---

## Phase 2: 애플리케이션 배포 (Day 3-4)

### 학습 목표
- Deployment 이해와 활용
- 애플리케이션 업데이트 전략

### Day 3: Deployment 기초
```bash
# nginx Deployment 생성
kubectl apply -f phase2-deployment/01-simple-deployment.yaml

# Replica 조정
kubectl scale deployment my-first-app --replicas=3 -n test-namespace
```

### Day 4: Rolling Update
```bash
# 업데이트 실습
kubectl apply -f phase2-deployment/02-rolling-update.yaml

# 이미지 업데이트
kubectl set image deployment/update-demo nginx=nginx:1.24-alpine -n test-namespace

# 롤백
kubectl rollout undo deployment/update-demo -n test-namespace
```

---

## Phase 3: 서비스 노출 (Day 5)

### 학습 목표
- Service 타입별 특징 이해
- Ingress를 통한 HTTP 라우팅

### 실습 내용
```bash
# Service 생성
kubectl apply -f phase3-networking/

# NodePort 테스트
curl http://localhost:30080

# Ingress 설정
# /etc/hosts 파일에 추가:
# 127.0.0.1 myapp.local k3s.local
```

---

## Phase 4: 설정 관리 (Day 6-7)

### 학습 목표
- ConfigMap으로 설정 외부화
- Secret으로 민감 정보 관리

### Day 6: ConfigMap
```bash
# ConfigMap 생성
kubectl apply -f phase4-configuration/01-configmap-basic.yaml

# 환경변수 확인
kubectl exec deployment/app-with-env -n test-namespace -- env
```

### Day 7: Secret
```bash
# Secret 생성
kubectl apply -f phase4-configuration/02-secret-basic.yaml

# Secret 값 확인
kubectl get secret app-secret -n test-namespace -o jsonpath='{.data.username}' | base64 -d
```

---

## Phase 5: 데이터 영속성 (Day 8-9)

### 학습 목표
- Volume 타입 이해
- PVC/PV 활용
- StatefulSet 기초

### Day 8: Volume 기초
```bash
# Storage 실습
kubectl apply -f phase5-storage/01-storage-basic.yaml

# PVC 상태 확인
kubectl get pvc -n test-namespace
```

### Day 9: StatefulSet
```bash
# StatefulSet 배포
kubectl get pods -l app=nginx-sts -n test-namespace

# 각 Pod의 PVC 확인
kubectl get pvc -n test-namespace | grep www-web-stateful
```

---

## Phase 6: 실전 3-Tier 프로젝트 (Day 10-12)

### 학습 목표
- 실제 애플리케이션 배포
- 멀티 티어 아키텍처 구현

### Day 10: Database
```bash
# MySQL 배포
kubectl apply -f phase6-project/01-database-simple.yaml

# 연결 테스트
kubectl exec -it mysql-client -n test-namespace -- mysql -h mysql-service -uroot -p
```

### Day 11: Backend
```bash
# Spring Boot 배포
kubectl apply -f phase6-project/02-backend-simple.yaml

# API 테스트
curl http://localhost:30088/actuator/health
```

### Day 12: Frontend & 통합
```bash
# Frontend 배포
kubectl apply -f phase6-project/03-frontend-simple.yaml

# 전체 시스템 테스트
curl http://localhost:30080
```

---

## Phase 7: 운영 필수 (Day 13-14)

### 학습 목표
- Health Check 구성
- 로그 수집과 분석
- 트러블슈팅 능력

### Day 13: 모니터링
```bash
# 운영 도구 배포
kubectl apply -f phase7-operations/01-operations-basic.yaml

# 리소스 모니터링
kubectl top nodes
kubectl top pods -n test-namespace
```

### Day 14: 트러블슈팅
```bash
# 디버그 Pod 사용
kubectl exec -it debug-pod -n test-namespace -- bash

# 로그 분석
kubectl logs deployment/app-with-logging -c log-collector -n test-namespace
```

---

## 일일 체크리스트

### Week 1 (기초)
- [ ] Day 1: Kubernetes 아키텍처 이해
- [ ] Day 2: 첫 Pod 실행 성공
- [ ] Day 3: Deployment 생성과 스케일링
- [ ] Day 4: Rolling Update 실습
- [ ] Day 5: Service와 Ingress 구성
- [ ] Day 6: ConfigMap 활용
- [ ] Day 7: Secret 관리

### Week 2 (실전)
- [ ] Day 8: Volume과 PVC 실습
- [ ] Day 9: StatefulSet 이해
- [ ] Day 10: MySQL 데이터베이스 구축
- [ ] Day 11: Spring Boot 백엔드 배포
- [ ] Day 12: 3-Tier 통합 완성
- [ ] Day 13: 모니터링 구성
- [ ] Day 14: 트러블슈팅 연습

---

## 실습 환경

### 디렉토리 구조
```
manifests/
├── phase1-first-steps/      # 기초 (2 파일)
├── phase2-deployment/        # 배포 (2 파일)
├── phase3-networking/        # 네트워킹 (3 파일)
├── phase4-configuration/     # 설정 (2 파일)
├── phase5-storage/          # 스토리지 (1 파일)
├── phase6-project/          # 프로젝트 (3 파일)
└── phase7-operations/       # 운영 (1 파일)
```

### 전체 실습 명령
```bash
# 순차 실행
for i in {1..7}; do
  echo "Phase $i 시작"
  kubectl apply -f manifests/phase$i-*/
  read -p "Enter를 눌러 계속..."
done

# 전체 정리
kubectl delete namespace test-namespace
```

---

## 학습 팁

### 효과적인 학습법
1. **매일 실습**: 이론보다 실습 중심
2. **에러 기록**: 문제 해결 과정 문서화
3. **반복 연습**: 명령어 암기보다 이해
4. **점진적 확장**: 간단한 것부터 시작

### 트러블슈팅 기본
```bash
# Pod 문제 해결
kubectl describe pod <pod-name> -n test-namespace
kubectl logs <pod-name> -n test-namespace --previous

# Service 연결 확인
kubectl get endpoints -n test-namespace

# Event 확인
kubectl get events -n test-namespace --sort-by='.lastTimestamp'
```

---

## 참고 자료

### 필수 문서
- [K3s 공식 문서](https://docs.k3s.io/)
- [Kubernetes 공식 튜토리얼](https://kubernetes.io/docs/tutorials/)
- [kubectl 치트시트](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)

### 추천 도구
- **k9s**: 터미널 UI
- **Lens**: Desktop IDE
- **stern**: 멀티 Pod 로그

---

## 달성 목표

### 2주 후 가능한 수준
- K3s 클러스터 독립 운영
- 3-Tier 애플리케이션 배포
- 기본적인 트러블슈팅
- ConfigMap/Secret 관리
- PVC를 통한 데이터 영속성

### 다음 단계 (Advanced)
- Prometheus/Grafana 모니터링
- Loki Stack 로깅
- ArgoCD GitOps
- Service Mesh
- Multi-cluster 관리

---

*작성일: 2025-01-10*
*구성: 7-Phase 실전 학습 과정*
