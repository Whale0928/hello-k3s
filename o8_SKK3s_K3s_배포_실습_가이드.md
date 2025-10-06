# SKK3s K3s 배포 실습 가이드

Spring Boot + Kotlin 애플리케이션을 K3s에 배포하는 학습용 가이드입니다.

## 📁 매니페스트 파일 구조

```
manifests/
├── skk3s-configmap.yaml     # ConfigMap: 환경 변수 (비민감 정보)
├── skk3s-secret.yaml         # Secret: 민감 정보 (DB 비밀번호, API 키)
├── skk3s-deployment.yaml     # Deployment: Pod 생성 및 관리
└── skk3s-service.yaml        # Service: 네트워크 접근 제공
```

### 각 파일의 역할

#### 1. ConfigMap (`skk3s-configmap.yaml`)
- **목적**: 환경별 설정을 코드와 분리
- **내용**: Spring Profile, 서버 포트, 타임존 등
- **장점**: 이미지 재빌드 없이 설정 변경 가능

```yaml
# 예시
data:
  SPRING_PROFILES_ACTIVE: "production"
  SERVER_PORT: "8080"
```

#### 2. Secret (`skk3s-secret.yaml`)
- **목적**: 민감한 정보 안전 관리
- **내용**: DB 비밀번호, Redis 비밀번호, API 키, JWT Secret
- **주의**: 학습용으로 하드코딩되어 있으며, 실무에서는 별도 관리 필요

```yaml
# 예시 (stringData 사용)
stringData:
  DB_PASSWORD: "sample_password_123"
  API_KEY: "sk-1234567890..."
```

#### 3. Deployment (`skk3s-deployment.yaml`)
- **목적**: 애플리케이션 실행 및 관리
- **기능**:
  - Pod 복제본 관리 (replicas: 2)
  - 롤링 업데이트 전략
  - 헬스체크 (Liveness, Readiness, Startup Probe)
  - 리소스 제한 (CPU/메모리)
  - ConfigMap/Secret 환경 변수 주입

#### 4. Service (`skk3s-service.yaml`)
- **목적**: Pod에 네트워크 접근 제공
- **타입**: NodePort (학습용)
- **포트**: 30080 (외부 접근)
- **기능**: 로드밸런싱, 서비스 디스커버리

---

## 🆚 Deployment vs StatefulSet

### Deployment (현재 사용)
```yaml
# 무상태 애플리케이션
- Pod 이름: skk3s-deployment-xxx-yyy (랜덤)
- 시작 순서: 동시 다발적
- 스토리지: 공유 또는 임시
- 용도: REST API, 웹서버
```

### StatefulSet (회사 코드에서 사용하는 경우)
```yaml
# 상태 유지 애플리케이션
- Pod 이름: backend-0, backend-1, backend-2 (고정)
- 시작 순서: 0 → 1 → 2 순차적
- 스토리지: Pod별 전용 PersistentVolume
- 용도: DB, Kafka, Redis 클러스터
```

**Q: API 서버인데 StatefulSet을 쓰는 이유?**
- 순차적 배포 (더 안전한 롤링 업데이트)
- Pod 이름 고정 (로그 추적 용이)
- 특정 Pod 직접 통신 필요
- 미래 확장성 (캐시/세션 저장 예정)

**SKK3s는 완전 무상태 API이므로 Deployment가 적합합니다.**

---

## 🚀 배포 순서

### 1단계: 도커 이미지 빌드

```bash
cd SKK3s

# 로컬에서 이미지 빌드
docker build -t skk3s:latest .

# 이미지 확인
docker images | grep skk3s
```

**Dockerfile 특징:**
- Multi-stage 빌드 (빌드 환경 + 실행 환경 분리)
- Gradle 의존성 캐싱 최적화
- 보안: non-root 사용자 (spring:spring)
- JVM 최적화: 컨테이너 메모리 제한 인식

### 2단계: K3s에 이미지 로드

#### 방법 A: K3s containerd에 직접 로드 (간단)

```bash
# 이미지를 tar로 저장
docker save skk3s:latest -o skk3s.tar

# K3s containerd에 로드
sudo k3s ctr images import skk3s.tar

# 확인
sudo k3s ctr images ls | grep skk3s
```

#### 방법 B: 로컬 레지스트리 사용 (권장, 실무 패턴)

```bash
# 로컬 레지스트리 실행
docker run -d -p 5000:5000 --restart=always --name registry registry:2

# 이미지 태깅
docker tag skk3s:latest localhost:5000/skk3s:latest

# 레지스트리에 푸시
docker push localhost:5000/skk3s:latest

# skk3s-deployment.yaml의 image 변경:
# image: localhost:5000/skk3s:latest
```

### 3단계: 매니페스트 적용

```bash
# 방법 1: 개별 파일 순차 적용 (권장, 의존성 명확)
kubectl apply -f manifests/skk3s-configmap.yaml
kubectl apply -f manifests/skk3s-secret.yaml
kubectl apply -f manifests/skk3s-deployment.yaml
kubectl apply -f manifests/skk3s-service.yaml

# 방법 2: 디렉토리 전체 적용 (간편)
kubectl apply -f manifests/

# 배포 확인
kubectl get all -l app=skk3s
```

**왜 순서가 중요한가?**
- ConfigMap/Secret → Deployment가 참조
- Deployment → Service가 선택 (selector)

### 4단계: 배포 검증

```bash
# Pod 상태 확인
kubectl get pods -l app=skk3s

# 출력 예시:
# NAME                               READY   STATUS    RESTARTS   AGE
# skk3s-deployment-7d8f9c-abcd       1/1     Running   0          30s
# skk3s-deployment-7d8f9c-efgh       1/1     Running   0          30s

# Pod 로그 확인 (실시간)
kubectl logs -f deployment/skk3s-deployment

# Service 확인
kubectl get svc skk3s-service

# 헬스체크 테스트
curl http://localhost:30080/health   # 출력: ok
curl http://localhost:30080/ping     # 출력: pong
```

---

## 🔧 환경 변수 관리 전략

### ConfigMap vs Secret 구분

| 항목 | ConfigMap | Secret |
|------|-----------|--------|
| **용도** | 일반 설정 | 민감 정보 |
| **예시** | Spring Profile, 포트, 타임존 | DB 비밀번호, API 키 |
| **인코딩** | 평문 | base64 (암호화 아님) |
| **Git 관리** | ✓ 커밋 가능 | ✗ .gitignore 추가 |

### Deployment에서 환경 변수 주입 방법

```yaml
# 방법 1: 전체 주입 (권장)
envFrom:
- configMapRef:
    name: skk3s-config
- secretRef:
    name: skk3s-secret

# 방법 2: 개별 선택
env:
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: skk3s-secret
      key: DB_PASSWORD
- name: CUSTOM_VAR
  value: "직접 입력 값"
```

### 실무 패턴: .env 파일 → Secret

```bash
# 1. .env 파일 준비 (프라이빗 서브모듈)
cat > .env <<EOF
DB_PASSWORD=real_password
REDIS_PASSWORD=redis_pass
API_KEY=sk-real-key
EOF

# 2. Secret 생성
kubectl create secret generic skk3s-secret \
  --from-env-file=.env

# 3. Secret 확인
kubectl get secret skk3s-secret -o yaml
```

---

## 📋 주요 명령어

### 배포 관리

```bash
# 재배포 (이미지 업데이트 후)
kubectl rollout restart deployment/skk3s-deployment

# 롤아웃 상태 확인
kubectl rollout status deployment/skk3s-deployment

# 롤백 (이전 버전으로)
kubectl rollout undo deployment/skk3s-deployment

# 특정 버전으로 롤백
kubectl rollout undo deployment/skk3s-deployment --to-revision=2

# 롤아웃 히스토리 (최근 10개 버전)
kubectl rollout history deployment/skk3s-deployment
```

### 스케일링

```bash
# 수동 스케일링 (Pod 개수 조정)
kubectl scale deployment/skk3s-deployment --replicas=3

# Auto Scaling (HPA: Horizontal Pod Autoscaler)
kubectl autoscale deployment skk3s-deployment \
  --cpu-percent=70 \
  --min=1 \
  --max=5

# HPA 상태 확인
kubectl get hpa
```

### 설정 변경

```bash
# ConfigMap 수정
kubectl edit configmap skk3s-config

# Pod 재시작 (설정 반영)
kubectl rollout restart deployment/skk3s-deployment

# 또는: Secret 수정
kubectl edit secret skk3s-secret
```

### 디버깅

```bash
# Pod 내부 접속
kubectl exec -it deployment/skk3s-deployment -- /bin/sh

# 특정 Pod 로그
kubectl logs <pod-name>

# 이전 컨테이너 로그 (재시작된 경우)
kubectl logs <pod-name> --previous

# 모든 이벤트 확인 (최신순)
kubectl get events --sort-by='.lastTimestamp'

# 리소스 사용량 (Metrics Server 필요)
kubectl top pod -l app=skk3s
kubectl top node
```

### 정리

```bash
# 방법 1: 개별 삭제
kubectl delete -f manifests/skk3s-service.yaml
kubectl delete -f manifests/skk3s-deployment.yaml
kubectl delete -f manifests/skk3s-secret.yaml
kubectl delete -f manifests/skk3s-configmap.yaml

# 방법 2: label selector로 일괄 삭제
kubectl delete all -l app=skk3s
kubectl delete configmap,secret -l app=skk3s

# 방법 3: 디렉토리 전체
kubectl delete -f manifests/
```

---

## 🔥 트러블슈팅

### ImagePullBackOff 에러

```bash
# 원인: K3s가 이미지를 찾을 수 없음
# 해결 방법:

# 1. 이미지 로드 확인
sudo k3s ctr images ls | grep skk3s

# 2. Deployment의 imagePullPolicy 확인
kubectl get deployment skk3s-deployment -o yaml | grep imagePullPolicy

# 3. imagePullPolicy를 IfNotPresent로 변경
kubectl edit deployment skk3s-deployment
```

### CrashLoopBackOff 에러

```bash
# 원인: 컨테이너가 시작 후 계속 재시작됨
# 해결 방법:

# 1. 로그 확인
kubectl logs -f <pod-name>
kubectl describe pod <pod-name>

# 2. Spring Boot 시작 시간이 오래 걸리는 경우
# skk3s-deployment.yaml에서 startupProbe 수정:
startupProbe:
  failureThreshold: 20  # 12 → 20 (100초 대기)

# 3. 환경 변수 확인
kubectl exec -it <pod-name> -- env | grep -E "DB|REDIS"
```

### Pending 상태

```bash
# 원인: 리소스 부족 또는 스케줄링 실패
# 해결 방법:

# 1. Pod 상세 정보 확인
kubectl describe pod <pod-name>

# 2. 노드 리소스 확인
kubectl top node
kubectl describe node

# 3. 리소스 요청량 줄이기
# skk3s-deployment.yaml에서 resources.requests 조정:
resources:
  requests:
    memory: "128Mi"  # 256Mi → 128Mi
    cpu: "100m"      # 250m → 100m
```

### ConfigMap/Secret 변경이 반영 안 됨

```bash
# 원인: Pod는 시작 시에만 환경 변수를 로드함
# 해결 방법:

# ConfigMap/Secret 변경 후 Pod 재시작 필수
kubectl rollout restart deployment/skk3s-deployment

# 또는 Rolling Update 트리거
kubectl patch deployment skk3s-deployment \
  -p '{"spec":{"template":{"metadata":{"annotations":{"restarted-at":"'$(date +%s)'"}}}}}'
```

---

## 🎓 학습 포인트

### 1. 파일 분리의 이점

```
✓ 관심사 분리: 설정 / 민감정보 / 실행 / 네트워크
✓ 재사용성: ConfigMap은 여러 Deployment에서 공유 가능
✓ 보안: Secret만 별도 관리 (Git 제외)
✓ 유지보수: 각 파일의 역할이 명확
```

### 2. Deployment 롤링 업데이트 전략

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1        # 새 Pod 1개 먼저 생성
    maxUnavailable: 0  # 기존 Pod는 새 Pod Ready 후 종료

# 실제 동작:
# 1. 새 버전 Pod 1개 생성 (총 3개)
# 2. 새 Pod Ready 확인
# 3. 기존 Pod 1개 종료 (총 2개)
# 4. 반복
```

**장점:**
- 무중단 배포 (항상 2개 이상 Pod 실행)
- 새 버전 문제 시 빠른 롤백
- 리소스 효율적 (최대 1개 추가 Pod)

### 3. 헬스체크 3종류

| Probe | 목적 | 실패 시 동작 |
|-------|------|-------------|
| **Liveness** | 살아있는가? | 컨테이너 재시작 |
| **Readiness** | 트래픽 받을 준비? | Service에서 제거 |
| **Startup** | 시작 완료? | Liveness/Readiness 비활성화 |

**Spring Boot 권장 설정:**
```yaml
startupProbe:
  initialDelaySeconds: 10
  failureThreshold: 12  # 최대 60초 대기

livenessProbe:
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  initialDelaySeconds: 10
  periodSeconds: 5
```

---

## 🚀 다음 단계

### 1. Ingress 추가 (도메인 기반 라우팅)
```yaml
# skk3s.local → skk3s-service
# Service를 ClusterIP로 변경
# Ingress로 외부 접근 제공
```

### 2. Persistent Volume (영구 저장소)
```yaml
# 로그 파일 영구 저장
# 파일 업로드 저장소
```

### 3. Horizontal Pod Autoscaler (오토스케일링)
```bash
# CPU 70% 초과 시 자동 스케일 아웃
kubectl autoscale deployment skk3s-deployment \
  --cpu-percent=70 --min=2 --max=10
```

### 4. ConfigMap 외부화 (GitOps)
```
# 환경별 설정 분리
manifests/
├── base/             # 공통
├── dev/              # 개발
├── staging/          # 스테이징
└── production/       # 프로덕션

# Kustomize 사용
```

### 5. CI/CD 연동 (GitHub Actions)
```yaml
# .github/workflows/deploy.yml
# 1. Docker 이미지 빌드
# 2. 레지스트리 푸시
# 3. kubectl apply
# 4. 롤아웃 확인
```

---

## 📚 참고 자료

- [Kubernetes 공식 문서](https://kubernetes.io/docs/)
- [K3s 공식 문서](https://docs.k3s.io/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- 프로젝트 내 관련 문서:
  - `o1_쿠버네티스_아키텍처_이해하기.md`
  - `o4_쿠버네티스_매니페스트_이해하기.md`
  - `o7_k3s_설치_가이드_및_트러블슈팅.md`
