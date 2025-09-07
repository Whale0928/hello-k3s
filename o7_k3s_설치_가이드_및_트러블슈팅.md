# K3s 설치 가이드 및 트러블슈팅 (2025.09)

## 📌 최신 버전 정보
- **최신 안정화 버전:** v1.33.4 (2025년 8월)
- **LTS 버전:** v1.30.14, v1.31.12
- **주요 변경사항:** Containerd 2.0 포함 (v1.31.6+k3s1부터)

## 🚀 K3s 설치 가이드

### 1. 시스템 요구사항 확인
```bash
# 최소 요구사항
# CPU: 1 core (2+ 권장)
# RAM: 512MB (1GB+ 권장) 
# Disk: 200MB (1GB+ 권장)
# 미니PC N100 16GB는 충분함

# OS 확인 (Ubuntu 22.04/24.04 권장)
cat /etc/os-release

# 커널 버전 확인 (4.x 이상)
uname -r

# 아키텍처 확인
dpkg --print-architecture
```

### 2. 사전 준비 작업
```bash
# 1. 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# 2. 필수 패키지 설치
sudo apt install -y curl wget apt-transport-https ca-certificates software-properties-common

# 3. 방화벽 설정 (UFW 사용 시)
sudo ufw allow 6443/tcp  # K3s API Server
sudo ufw allow 10250/tcp # Kubelet metrics
sudo ufw allow 10251/tcp # Scheduler
sudo ufw allow 10252/tcp # Controller manager
sudo ufw allow 2379:2380/tcp # etcd (HA 구성 시)
sudo ufw allow 30000:32767/tcp # NodePort 서비스
sudo ufw allow 8472/udp  # Flannel VXLAN
sudo ufw reload

# 4. Swap 비활성화 (권장)
sudo swapoff -a
sudo sed -i '/ swap / s/^/#/' /etc/fstab

# 5. 커널 모듈 로드
sudo modprobe br_netfilter
sudo modprobe overlay

# 6. 커널 파라미터 설정
cat <<EOF | sudo tee /etc/sysctl.d/k3s.conf
net.bridge.bridge-nf-call-iptables = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward = 1
EOF
sudo sysctl --system

# 7. Cgroup 설정 확인 (SystemD 기반)
sudo systemctl status systemd-logind
```

### 3. K3s 설치

#### 단일 노드 설치 (미니PC 권장)
```bash
# 기본 설치 (가장 간단)
curl -sfL https://get.k3s.io | sh -

# 특정 버전 설치
curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=v1.33.4+k3s1 sh -

# 커스텀 설정과 함께 설치
curl -sfL https://get.k3s.io | sh -s - \
  --write-kubeconfig-mode 644 \
  --disable traefik \
  --node-name "minipc-master"

# SQLite 대신 외부 DB 사용 (선택사항)
curl -sfL https://get.k3s.io | sh -s - \
  --datastore-endpoint="mysql://username:password@tcp(hostname:3306)/database"
```

#### 멀티 노드 설치
```bash
# Master 노드
curl -sfL https://get.k3s.io | sh -s - \
  --cluster-init \
  --write-kubeconfig-mode 644

# 토큰 확인
sudo cat /var/lib/rancher/k3s/server/node-token

# Worker 노드
curl -sfL https://get.k3s.io | K3S_URL=https://<MASTER_IP>:6443 \
  K3S_TOKEN=<NODE_TOKEN> sh -
```

### 4. 설치 확인
```bash
# K3s 서비스 상태 확인
sudo systemctl status k3s

# 노드 상태 확인
sudo k3s kubectl get nodes

# 시스템 파드 확인
sudo k3s kubectl get pods -A

# 클러스터 정보
sudo k3s kubectl cluster-info

# K3s 버전 확인
k3s --version
```

### 5. Kubectl 설정
```bash
# kubeconfig 복사 (일반 사용자)
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $(id -u):$(id -g) ~/.kube/config

# kubectl 별도 설치 (선택사항)
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# 자동완성 설정
echo 'source <(kubectl completion bash)' >> ~/.bashrc
echo 'alias k=kubectl' >> ~/.bashrc
echo 'complete -F __start_kubectl k' >> ~/.bashrc
source ~/.bashrc
```

## 🔧 트러블슈팅 Q&A

### Q1: "Job for k3s.service failed" 에러 발생
**증상:**
```
Job for k3s.service failed because the control process exited with error code.
```

**해결방법:**
```bash
# 1. 상세 로그 확인
sudo journalctl -xe -u k3s
sudo systemctl status k3s

# 2. 기존 설치 제거 후 재설치
sudo /usr/local/bin/k3s-uninstall.sh
curl -sfL https://get.k3s.io | sh -

# 3. 바이너리 권한 확인
sudo chmod +x /usr/local/bin/k3s
ls -la /usr/local/bin/k3s
```

### Q2: SELinux 관련 에러 (RHEL/CentOS/Rocky)
**증상:**
```
Failed to apply container_runtime_exec_t to /usr/local/bin/k3s
```

**해결방법:**
```bash
# SELinux 패키지 설치
sudo yum install -y container-selinux selinux-policy-base
sudo yum install -y https://rpm.rancher.io/k3s/latest/common/centos/9/noarch/k3s-selinux-1.6-1.el9.noarch.rpm

# 또는 SELinux 임시 비활성화
sudo setenforce 0
sudo sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
```

### Q3: iptables 버전 문제
**증상:**
```
iptables v1.8.0-1.8.4 detected with bugs
```

**해결방법:**
```bash
# iptables 버전 확인
iptables --version

# legacy 모드로 전환
sudo update-alternatives --set iptables /usr/sbin/iptables-legacy
sudo update-alternatives --set ip6tables /usr/sbin/ip6tables-legacy

# nftables 모드 문제 시
sudo apt install -y iptables-legacy
sudo update-alternatives --config iptables
```

### Q4: Worker 노드가 조인되지 않음
**증상:**
```
Worker node installs, but doesn't show up with kubectl get nodes
```

**해결방법:**
```bash
# 1. 방화벽 확인 (Master에서)
sudo ufw status
ping <MASTER_IP>
telnet <MASTER_IP> 6443

# 2. 토큰 재확인
sudo cat /var/lib/rancher/k3s/server/node-token

# 3. Worker 노드 로그 확인
sudo journalctl -u k3s-agent -f

# 4. DNS 대신 IP 사용
# hostname 대신 IP 주소 직접 사용
K3S_URL=https://192.168.1.100:6443

# 5. /etc/hosts 설정
echo "192.168.1.100 master-node" | sudo tee -a /etc/hosts
```

### Q5: Unauthorized 에러
**증상:**
```
You must be logged in to the server (Unauthorized)
```

**해결방법:**
```bash
# 1. kubeconfig 권한 확인
ls -la ~/.kube/config
chmod 600 ~/.kube/config

# 2. kubeconfig 재설정
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $(id -u):$(id -g) ~/.kube/config

# 3. 환경변수 설정
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
```

### Q6: Containerd 관련 에러
**증상:**
```
Failed to start containerd: timeout waiting for containerd
```

**해결방법:**
```bash
# 1. Containerd 상태 확인
sudo k3s crictl info

# 2. Containerd 설정 초기화
sudo rm -rf /var/lib/rancher/k3s/agent/etc/containerd/config.toml
sudo systemctl restart k3s

# 3. Docker 충돌 확인 (Docker가 설치된 경우)
sudo systemctl stop docker
sudo systemctl disable docker
```

### Q7: Cgroup 에러 (Raspberry Pi/ARM)
**증상:**
```
Failed to find cgroup
```

**해결방법:**
```bash
# 1. cgroup 활성화 (/boot/firmware/cmdline.txt 수정)
cgroup_memory=1 cgroup_enable=memory

# 2. 재부팅
sudo reboot

# 3. cgroup 확인
cat /proc/cgroups
```

### Q8: 디스크 공간 부족
**증상:**
```
No space left on device
```

**해결방법:**
```bash
# 1. 디스크 사용량 확인
df -h
du -sh /var/lib/rancher/k3s/*

# 2. 이미지 정리
sudo k3s crictl rmi --prune

# 3. 로그 정리
sudo journalctl --vacuum-size=100M
```

### Q9: DNS 해석 실패
**증상:**
```
couldn't get current server API group list
```

**해결방법:**
```bash
# 1. CoreDNS 파드 확인
kubectl get pods -n kube-system | grep coredns

# 2. DNS 설정 확인
cat /etc/resolv.conf

# 3. CoreDNS 재시작
kubectl delete pods -n kube-system -l k8s-app=kube-dns
```

### Q10: 메모리 부족으로 인한 파드 Evicted
**증상:**
```
The node was low on resource: memory
```

**해결방법:**
```bash
# 1. 메모리 사용량 확인
free -h
kubectl top nodes

# 2. Eviction 임계값 조정
curl -sfL https://get.k3s.io | sh -s - \
  --kubelet-arg="eviction-hard=memory.available<100Mi"

# 3. 불필요한 파드 정리
kubectl delete pods --field-selector status.phase=Failed -A
```

### Q11: etcd 관련 에러 (HA 구성)
**증상:**
```
etcd cluster is unavailable or misconfigured
```

**해결방법:**
```bash
# 1. etcd 상태 확인
sudo k3s etcd-snapshot save --name debug
sudo k3s etcd-snapshot list

# 2. etcd 멤버 확인
ETCDCTL_API=3 etcdctl \
  --cacert=/var/lib/rancher/k3s/server/tls/etcd/server-ca.crt \
  --cert=/var/lib/rancher/k3s/server/tls/etcd/client.crt \
  --key=/var/lib/rancher/k3s/server/tls/etcd/client.key \
  member list
```

### Q12: 네트워크 플러그인 (Flannel) 에러
**증상:**
```
Failed to create pod sandbox: network plugin is not ready
```

**해결방법:**
```bash
# 1. Flannel 파드 확인
kubectl get pods -n kube-system | grep flannel

# 2. CNI 설정 확인
ls -la /etc/cni/net.d/

# 3. Flannel 재설정
kubectl delete pods -n kube-system -l app=flannel
```

### Q13: 시간 동기화 문제
**증상:**
```
x509: certificate has expired or is not yet valid
```

**해결방법:**
```bash
# 1. 시간 확인
timedatectl status

# 2. NTP 동기화
sudo apt install -y chrony
sudo systemctl enable --now chrony
sudo chronyc sources

# 3. 수동 시간 설정
sudo timedatectl set-time "2025-09-06 10:00:00"
```

### Q14: User Namespace 에러 (RHEL7/CentOS7)
**증상:**
```
User namespaces disabled; add 'user_namespace.enable=1' to boot
```

**해결방법:**
```bash
# 1. 현재 설정 확인
cat /proc/sys/user/max_user_namespaces

# 2. GRUB 설정 수정
sudo vi /etc/default/grub
# GRUB_CMDLINE_LINUX에 추가: user_namespace.enable=1

# 3. GRUB 업데이트
sudo grub2-mkconfig -o /boot/grub2/grub.cfg
sudo reboot
```

### Q15: AppArmor 관련 에러 (Ubuntu)
**증상:**
```
Failed to setup apparmor profile
```

**해결방법:**
```bash
# 1. AppArmor 상태 확인
sudo aa-status

# 2. AppArmor 프로파일 재로드
sudo systemctl reload apparmor

# 3. 문제가 지속되면 임시 비활성화
sudo systemctl stop apparmor
sudo systemctl disable apparmor
```

## 📝 설치 후 체크리스트

```bash
# 1. 클러스터 상태 종합 확인
echo "=== Node Status ==="
kubectl get nodes -o wide

echo "=== System Pods ==="
kubectl get pods -A

echo "=== K3s Service ==="
sudo systemctl status k3s --no-pager

echo "=== Cluster Info ==="
kubectl cluster-info

echo "=== Storage Classes ==="
kubectl get storageclass

echo "=== Version Info ==="
kubectl version --short
```

## 🔄 K3s 제거 방법

```bash
# Master 노드
sudo /usr/local/bin/k3s-uninstall.sh

# Worker 노드
sudo /usr/local/bin/k3s-agent-uninstall.sh

# 데이터 완전 삭제
sudo rm -rf /var/lib/rancher/k3s
sudo rm -rf /etc/rancher/k3s
sudo rm -rf ~/.kube
```

## 💡 팁과 모범 사례

1. **미니PC 단일 노드 운영 시**
   - `--disable traefik` 옵션으로 리소스 절약
   - SQLite 기본 사용 (etcd 불필요)
   - 로컬 스토리지 활용

2. **프로덕션 준비**
   - 정기적인 etcd 백업 설정
   - 모니터링 도구 설치 (Prometheus, Grafana)
   - 로그 수집 설정 (Fluentd, Loki)

3. **성능 최적화**
   - Swap 비활성화 필수
   - SSD 사용 권장
   - 불필요한 시스템 서비스 중지

4. **보안 강화**
   - kubeconfig 권한 관리 (600)
   - NetworkPolicy 설정
   - RBAC 구성
   - 정기적인 업데이트

## 📚 추가 리소스

- [K3s 공식 문서](https://docs.k3s.io/)
- [K3s GitHub](https://github.com/k3s-io/k3s)
- [K3s 릴리즈 노트](https://github.com/k3s-io/k3s/releases)
- [Rancher 커뮤니티 포럼](https://forums.rancher.com/c/k3s/)

---
*최종 업데이트: 2025년 9월 6일*
*K3s 버전: v1.33.4*