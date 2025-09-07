# Hello K3s - Spring Boot 개발자의 Kubernetes 학습 프로젝트

## 프로젝트 소개

Spring Boot 백엔드 개발자가 Kubernetes를 학습하고 실무에 적용하기 위한 문서화 프로젝트입니다.

**배경**
- 현재: GitHub Actions를 이용한 다중 리눅스 서버 롤링 배포 운영 중
- 목표: 컨테이너 오케스트레이션 도입으로 배포/운영 자동화 개선
- 환경: 미니PC(N100, 16GB RAM)를 활용한 홈랩 구축

**왜 이 프로젝트를 시작했나**
- 회사 DevOps 팀과의 효과적인 협업을 위한 K8s 지식 필요
- 기존 Spring Boot + Redis + MySQL + Next.js 스택을 K8s로 마이그레이션
- 온프레미스(미니PC)에서 클라우드까지 점진적 확장 계획

## 학습 문서 목록

### [o1_쿠버네티스_아키텍처_이해하기](./o1_쿠버네티스_아키텍처_이해하기.md)
여러 서버를 하나의 클러스터로 관리하는 개념, Control Plane과 Worker Node 구조, 단일 노드에서 멀티 클러스터까지의 진화 과정

### [o2_쿠버네티스_핵심_용어_정리](./o2_쿠버네티스_핵심_용어_정리.md)
Pod, Deployment, Service 등 필수 개념, 네트워킹, 스토리지, 설정 관리 용어 사전

### [o3_k3s와_k8s_비교_분석](./o3_k3s와_k8s_비교_분석.md)
경량 K3s를 선택한 이유, 리소스 사용량 비교(1GB vs 5-10GB), API 호환성 분석

### [o4_쿠버네티스_매니페스트_이해하기](./o4_쿠버네티스_매니페스트_이해하기.md)
YAML 매니페스트 작성법, 기본 구조(apiVersion, kind, metadata, spec), 자주 하는 실수와 해결법

### [o5_마스터_노드_이해하기](./o5_마스터_노드_이해하기.md)
Control Plane의 핵심 컴포넌트(API Server, Scheduler, Controller Manager, etcd), 고가용성 구성, 보안 및 모니터링

### [o6_워커_노드_이해하기](./o6_워커_노드_이해하기.md)
실제 애플리케이션이 실행되는 워커 노드의 구성요소(kubelet, kube-proxy, Container Runtime), 파드 실행 과정, 리소스 관리

### [o7_k3s_설치_가이드_및_트러블슈팅](./o7_k3s_설치_가이드_및_트러블슈팅.md)
K3s v1.33.4 설치 방법, 15가지 트러블슈팅 시나리오, 미니PC 환경 최적화

## 프로젝트 진행 상황

**현재 단계: 이론 학습 및 문서화 완료**
- [x] Kubernetes 아키텍처 이해
- [x] 핵심 용어 정리
- [x] K3s vs K8s 비교
- [x] 매니페스트 작성법
- [x] 마스터 노드 이해
- [x] 워커 노드 이해
- [x] 설치 가이드 작성

**다음 단계: 실습 환경 구축**
- [ ] 미니PC에 K3s 설치
- [ ] 첫 Pod 배포
- [ ] Spring Boot 애플리케이션 컨테이너화
- [ ] 데이터베이스 StatefulSet 구성
- [ ] 모니터링 시스템 구축

**최종 목표: 프로덕션 환경**
- [ ] 클라우드 워커 노드 추가 (하이브리드 구성)
- [ ] GitOps 파이프라인 구축
- [ ] 멀티 클러스터 관리

## 기술 스택

**현재 운영 중인 스택**
- Backend: Spring Boot (Java 17)
- Cache: Redis
- Database: MySQL
- Frontend: Next.js
- CI/CD: GitHub Actions
- Deployment: 수동 롤링 배포

**목표 인프라 스택**
- Container Orchestration: K3s
- Container Runtime: containerd
- Network: Flannel
- Storage: Local Path Provisioner
- Monitoring: Prometheus + Grafana
- GitOps: ArgoCD (예정)

## 하드웨어 환경

**미니PC 스펙**
- CPU: Intel N100 (4 cores)
- RAM: 16GB DDR4
- Storage: 512GB NVMe SSD
- Network: 1Gbps Ethernet
- OS: Ubuntu 22.04 LTS (예정)

**확장 계획**
1. Phase 1: 미니PC 단일 노드 (개발/테스트)
2. Phase 2: 클라우드 VM 추가 (스테이징)
3. Phase 3: 관리형 K8s 서비스 (프로덕션)

## 프로젝트 구조

```
hello-k3s/
├── README.md                              # 프로젝트 개요
├── o1_쿠버네티스_아키텍처_이해하기.md        # 이론: 전체 구조
├── o2_쿠버네티스_핵심_용어_정리.md          # 이론: 용어
├── o3_k3s와_k8s_비교_분석.md               # 이론: 비교
├── o4_쿠버네티스_매니페스트_이해하기.md      # 실습: YAML
├── o5_마스터_노드_이해하기.md              # 이론: Control Plane
├── o6_워커_노드_이해하기.md                # 이론: Worker Node
└── o7_k3s_설치_가이드_및_트러블슈팅.md      # 실습: 설치
```

## 학습 경로

```
1단계: 큰 그림 이해 (1-2일)
├── o1. 아키텍처 이해
└── o2. 용어 학습

2단계: 깊이 있는 이해 (2-3일)
├── o3. K3s vs K8s 비교
├── o5. 마스터 노드 이해
└── o6. 워커 노드 이해

3단계: 실습 준비 (1-2일)
└── o4. 매니페스트 이해

4단계: 실전 (1주)
├── o7. K3s 설치
└── 첫 애플리케이션 배포
```

## 참고 자료

- K3s 공식 문서: https://docs.k3s.io/
- Kubernetes 공식 문서: https://kubernetes.io/ko/docs/
- K3s GitHub: https://github.com/k3s-io/k3s

## 라이센스

이 프로젝트의 문서는 학습 목적으로 작성되었으며, 자유롭게 참고하실 수 있습니다.

---
작성 시작: 2025년 9월  
작성자: Spring Boot 개발자의 K8s 도전기