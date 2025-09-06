# K3s와 Kubernetes 차이점 분석

## 개요

K3s는 Rancher에서 개발한 경량화된 Kubernetes 배포판입니다. CNCF 인증을 받은 정식 Kubernetes 구현체로서, 표준 Kubernetes와 API 레벨에서 완전한 호환성을 제공합니다.

## 아키텍처 비교

### 표준 Kubernetes
표준 Kubernetes는 다음과 같은 분산 아키텍처를 가집니다:

**Control Plane (Master Node)**
- kube-apiserver: 독립 프로세스
- kube-scheduler: 독립 프로세스
- kube-controller-manager: 독립 프로세스
- etcd: 분산 키-값 저장소
- cloud-controller-manager: 클라우드 제공자별 구현

**Worker Node**
- kubelet: 노드 에이전트
- kube-proxy: 네트워크 프록시
- container runtime: containerd, CRI-O 등

### K3s
K3s는 단일 바이너리 구조로 모든 컴포넌트를 통합합니다:

**Server Process**
- 모든 Control Plane 컴포넌트를 하나의 프로세스로 통합
- SQLite를 기본 데이터스토어로 사용 (etcd 선택 가능)
- 내장된 containerd 사용

**Agent Process**
- kubelet과 kube-proxy를 통합
- 별도 설치 없이 container runtime 포함

## 주요 차이점

### 데이터 저장소

| 구분 | 표준 Kubernetes | K3s |
|------|----------------|-----|
| 기본 저장소 | etcd 클러스터 | SQLite |
| 고가용성 | etcd 3노드 이상 | etcd 또는 MySQL/PostgreSQL |
| 백업 방식 | etcd snapshot | 데이터베이스 파일 복사 |

### 네트워킹

**표준 Kubernetes**
- CNI 플러그인 별도 설치 필요
- Calico, Cilium, Weave Net 등 선택 가능
- 네트워크 정책 세밀 제어 가능

**K3s**
- Flannel을 기본 CNI로 내장
- VXLAN 백엔드 기본 사용
- 다른 CNI로 교체 가능

### 리소스 사용량

**메모리 사용량**
- 표준 Kubernetes: Control Plane 2-4GB, Worker 1-2GB
- K3s: Server 512MB-1GB, Agent 256MB

**CPU 사용량**
- 표준 Kubernetes: 2-4 vCPU (시스템 컴포넌트)
- K3s: 0.5-1 vCPU (통합 바이너리)

### 설치 및 관리

**표준 Kubernetes**
```bash
# kubeadm을 사용한 클러스터 초기화
kubeadm init --pod-network-cidr=10.244.0.0/16
kubeadm join [token-info]

# 또는 관리형 서비스
eksctl create cluster --name production
```

**K3s**
```bash
# 단일 명령어로 설치
curl -sfL https://get.k3s.io | sh -

# 업그레이드
curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=v1.28.4+k3s2 sh -
```

## 호환성

### API 호환성
K3s는 Kubernetes API와 100% 호환됩니다:
- kubectl 명령어 동일
- YAML 매니페스트 동일
- Helm 차트 동일
- Custom Resource Definitions 지원

### 워크로드 마이그레이션
```bash
# K3s에서 워크로드 추출
kubectl get all -A -o yaml > workloads.yaml

# 표준 Kubernetes에 배포
kubectl apply -f workloads.yaml
```

## 사용 사례별 권장사항

### K3s 적합 환경
- 개발 및 테스트 환경
- Edge Computing 및 IoT
- 리소스 제약 환경
- 단순한 워크로드
- 빠른 프로토타이핑

### 표준 Kubernetes 적합 환경
- 대규모 프로덕션 환경
- 엄격한 보안 요구사항
- 멀티 테넌시 환경
- 복잡한 네트워킹 요구사항
- 수천 노드 규모의 클러스터

## 제한사항

### K3s 제한사항
- Alpha 기능 일부 비활성화
- 클라우드 컨트롤러 미포함
- 기본 스토리지 클래스 제한적
- 대규모 클러스터에서 성능 제약

### 마이그레이션 고려사항
- 스토리지 클래스 재정의 필요
- LoadBalancer 서비스 동작 차이
- Ingress 컨트롤러 설정 차이
- 네트워크 정책 세밀도 차이

## 결론

K3s는 표준 Kubernetes와 기능적으로 동일하지만, 운영 복잡도와 리소스 사용량을 크게 줄인 배포판입니다. 개발 환경이나 소규모 배포에서는 K3s가 유리하며, 대규모 프로덕션 환경에서는 표준 Kubernetes의 확장성과 유연성이 필요합니다.